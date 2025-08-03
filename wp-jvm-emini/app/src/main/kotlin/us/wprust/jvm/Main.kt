package us.wprust.jvm

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatDesktop
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont
import com.formdev.flatlaf.util.SystemInfo
import com.jthemedetecor.OsThemeDetector
import getStoryDataAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import raven.extras.SlidePane
import raven.extras.SlidePaneTransition
import raven.modal.ModalDialog
import raven.modal.Toast
import raven.modal.component.SimpleModalBorder
import raven.modal.option.Option
import raven.modal.toast.option.ToastOption
import raven.modal.toast.option.ToastStyle
import uniffi.wp_epub_mini.downloadWattpadStory
import us.wprust.jvm.components.menubar.MenuBar
import us.wprust.jvm.scenes.*
import us.wprust.jvm.utils.CustomModalDialogs
import us.wprust.jvm.utils.LafAction
import us.wprust.jvm.utils.NetworkUtils
import java.awt.Color
import java.awt.Component
import java.awt.RenderingHints
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess


class Main internal constructor(detector: OsThemeDetector) : JFrame() {
    private val usingServer = NetworkUtils.COMMON_SERVERS.GOOGLE

    // --- ADDED ---
    private val downloadingPanel = DownloadingPanel()
    private val downloadResultPanel = DownloadResultPanel()
    private var tempEpubFile: File? = null
    private var successfulDownloadTitle: String? = null
    // -------------

    private val scope = MainScope()

    private val slidePane = SlidePane() // Creates ONE instance and stores it
    private val checkoutPanel = CheckoutPanel()
    private val checkingStoryPanel = CheckingStoryPanel()
    private val bookDownloaderPanel = BookDownloaderPanel()

    init {
        layout = null

        Toast.show(this, Toast.Type.INFO, "Welcome Back!", toastOption)

        slidePane.setBounds(10, 10 + 40 + 20, 600 - 20, 500 - (10 + 40 + 20 + 10))
        add(slidePane)

//        doProcess(67)

        bookDownloaderPanel.onDownload = { username, password ->
            if (bookDownloaderPanel.isLogin) {
                if (username.isNullOrBlank() || password.isNullOrBlank()) {
                    // This handles all error cases
                    when {
                        username.isNullOrBlank() && password.isNullOrBlank() -> {
                            showEmptyUsernameAndPasswordWithLoginEnabledDialog(this)
                        }

                        username.isNullOrBlank() -> {
                            showEmptyUsernameWithLoginEnabledDialog(this)
                        }

                        else -> { // Password must be blank
                            showEmptyPassWithLoginEnabledDialog(this)
                        }
                    }
                } else {
                    downloadStory(
                        storyId = bookDownloaderPanel.storyId,
                        isEmbedImages = bookDownloaderPanel.isEmbedImages,
                        isLogin = true,
                        username = username,
                        password = password
                    )
                }
            } else {
                downloadStory(
                    storyId = bookDownloaderPanel.storyId,
                    isEmbedImages = bookDownloaderPanel.isEmbedImages,
                    isLogin = false,
                    username = null,
                    password = null
                )
            }
        }

        // Setup callback to back in BookDownloaderPanel
        bookDownloaderPanel.onRestart = {
            //Itz bad doing here but
            checkoutPanel.rest()

            slidePane.addSlide(checkoutPanel, SlidePaneTransition.Type.BACK)
        }

        // --- ADDED: Set up callbacks for the result panel ---
        downloadResultPanel.onRestart = {

            //Itz bad doing here but
            checkoutPanel.rest()

            cleanupTempFile() // Clean up before going back
            slidePane.addSlide(checkoutPanel, SlidePaneTransition.Type.BACK)
        }

        downloadResultPanel.onSave = {
            promptToSaveFile()
        }
        // ---------------------------------------------------

        slidePane.addSlide(checkoutPanel)

        checkoutPanel.onCheckout = { storyInput, type ->
            if (storyInput.isBlank()) {
                val message = "You must input a valid Wattpad Input";
                ModalDialog.showModal(
                    this, CustomModalDialogs(
                        CustomModalDialogs.Type.WARNING, message, "Empty input field!", SimpleModalBorder.Option(
                            "Got it!", 0
                        ), null
                    ), modelOption
                )
            } else {
                if (NetworkUtils.isNetworkAvailable(usingServer)) {
                    //TODO: Do here

                    when (type) {
                        "Story ID" -> {
                            try {
                                val storyID = storyInput.toInt()

                                slidePane.addSlide(checkingStoryPanel, SlidePaneTransition.Type.FORWARD)

                                doProcess(storyID)

                            } catch (_: NumberFormatException) {
                                showNotANumberExceptionDialog(this)
                            } catch (e: Exception) {
                                showUnexpectedExceptionDialog(this, e.message)
                            }
                        }

                        "Story Link" -> {
                            try {
                                val storyLink = storyInput.trim()

                                val storyID = getStoryIdFromUrl(storyLink)

                                slidePane.addSlide(checkingStoryPanel, SlidePaneTransition.Type.FORWARD)

                                doProcess(storyID)

                            } catch (e: IllegalArgumentException) {
                                showNotAValidStoryLinkExceptionDialog(this)
                            } catch (e: Exception) {
                                showUnexpectedExceptionDialog(this, e.message)
                            }
                        }

                        else -> {
                            showSomeSeriousErrorOccurredDialog(this)
                        }
                    }

                } else {
                    showNoNetworkExceptionModalDialog(this);
                }
            }
        }

        val titleLabel = FlatLabel()
        titleLabel.setText("Wattpad Downloader")
        titleLabel.setBounds(0, 10, 600, 40)
        titleLabel.labelType = FlatLabel.LabelType.h0
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER)
        add(titleLabel)

        detector.registerListener { isDark: Boolean? ->
            SwingUtilities.invokeLater {
                LafAction.setTheme(false, this)
            }
        }

        LafAction.setIcon(this, detector)

        if (SystemInfo.isMacOS) {
            if (SystemInfo.isMacFullWindowContentSupported) {
                // expand window content into window title bar and make title bar transparent
                rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                rootPane.putClientProperty(
                    FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING,
                    FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING_LARGE
                )

                // hide window title
                if (SystemInfo.isJava_17_orLater) rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
                else setTitle(null)
            }
        }

        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                isAlwaysOnTop = false
                val confirmation = JOptionPane.showConfirmDialog(
                    null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION
                )
                if (confirmation == JOptionPane.YES_OPTION) {
                    // And cancel the scope to clean up coroutines
                    scope.cancel()
                    exitProcess(0)
                }
                isAlwaysOnTop = true
            }
        })

        jMenuBar = MenuBar.createMenuBar(this)
        setSize(616, 540)
        setTitle("Wattpad Downloader")
        setResizable(false)
        isVisible = true
        setLocationRelativeTo(null)
        setAlwaysOnTop(true)
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
    }

    /**
     * Checks if the input is a valid Wattpad story link and extracts the story ID.
     *
     * @param url The input string, which must be a URL containing "/story/".
     * @return The story ID as a non-null Int.
     * @throws IllegalArgumentException if the input is not a valid link or the ID is invalid.
     */
    fun getStoryIdFromUrl(url: String?): Int {
        val trimmedUrl = url?.trim()

        if (trimmedUrl.isNullOrBlank()) {
            throw IllegalArgumentException("URL cannot be null or empty.")
        }

        val storyUrlPattern = Regex("""/story/(\d+)""")

        // Chain of operations: find -> get string ID -> convert to Int
        return storyUrlPattern.find(trimmedUrl)
            ?.groupValues?.getOrNull(1) // Gets the ID as a String, e.g., "123456789"
            ?.toIntOrNull()             // Safely converts the String to an Int?
            ?: throw IllegalArgumentException("Input is not a valid Wattpad story link or contains an invalid ID.")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // --- ADDED HELPER FUNCTIONS ---
    private fun formatElapsedTime(millis: Long): String {
        val minutes = (millis / (1000 * 60)) % 60
        val seconds = (millis / 1000) % 60
        val remainingMillis = millis % 1000
        return String.format("%02d:%02d.%03d", minutes, seconds, remainingMillis)
    }

    private fun sanitizeFilename(name: String): String {
        val invalidChars = setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')
        return name.map { if (it in invalidChars) '_' else it }.joinToString("")
    }

    private fun cleanupTempFile() {
        tempEpubFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
        tempEpubFile = null
        successfulDownloadTitle = null
    }

    private fun promptToSaveFile() {
        if (tempEpubFile == null || !tempEpubFile!!.exists()) {
            Toast.show(
                this,
                Toast.Type.WARNING,
                "Temporary file not found. Please try the download again.",
                toastOption
            )
            return
        }

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save EPUB File"
        fileChooser.selectedFile = File(sanitizeFilename(successfulDownloadTitle ?: "story") + ".epub")
        fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("EPUB files", "epub")

        val userSelection = fileChooser.showSaveDialog(this)

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            var destinationFile = fileChooser.selectedFile
            if (!destinationFile.name.endsWith(".epub", ignoreCase = true)) {
                destinationFile = File(destinationFile.absolutePath + ".epub")
            }

            scope.launch(Dispatchers.IO) {
                try {
                    Files.move(tempEpubFile!!.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    tempEpubFile = null // The file has been moved, so the temp reference is invalid
                    withContext(Dispatchers.Main) {
                        Toast.show(this@Main, Toast.Type.SUCCESS, "File saved successfully!", toastOption)
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        showUnexpectedExceptionDialog(this@Main, "Failed to save file: ${e.message}")
                    }
                }
            }
        }
    }
    // ----------------------------


    // --- MODIFIED: The core download logic ---
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun downloadStory(
        storyId: Int, isEmbedImages: Boolean, isLogin: Boolean, username: String?, password: String?
    ) {
        // 1. Switch to the downloading slide
        slidePane.addSlide(downloadingPanel, SlidePaneTransition.Type.FORWARD)

        var timerJob: Job? = null
        val downloadStartTime = System.currentTimeMillis()

        scope.launch { // Main thread by default for UI setup
            try {
                // Start the UI timer
                timerJob = launch { // Child job on Main thread
                    while (isActive) {
                        val elapsed = System.currentTimeMillis() - downloadStartTime
                        downloadingPanel.updateTimer(formatElapsedTime(elapsed))
                        delay(67)
                    }
                }

                // Perform the actual download on a background thread
                val result = withContext(Dispatchers.IO) {
                    downloadingPanel.updateStatus("Creating temporary file...")
                    // Use modern Java NIO to create a secure temp file
                    tempEpubFile = Files.createTempFile("wp-story-", ".epub").toFile()

                    if (isLogin && !username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            downloadingPanel.updateStatus("Authenticating...")
                        }
                        uniffi.wp_epub_mini.login(username, password)
                    }

                    withContext(Dispatchers.Main) {
                        downloadingPanel.updateStatus("Downloading story content...")
                    }

                    // Call the Rust function to download the file to our temp path
                    downloadWattpadStory(
                        storyId = storyId,
                        embedImages = isEmbedImages,
                        concurrentRequests = 20uL, // Or your preferred value
                        outputPath = tempEpubFile!!.absolutePath
                    )
                }

                // Success: Stop timer and update UI
                timerJob?.cancel()
                val duration = System.currentTimeMillis() - downloadStartTime
                successfulDownloadTitle = result.title

                downloadResultPanel.showResult(
                    isSuccess = true,
                    title = "Download Successful!",
                    message = "Story '${result.title}' was downloaded in ${formatElapsedTime(duration)}."
                )
                slidePane.addSlide(downloadResultPanel, SlidePaneTransition.Type.FORWARD)

                // Automatically prompt the user to save
                promptToSaveFile()

            } catch (e: Exception) {
                // Error: Stop timer, clean up, and show error screen
                timerJob?.cancel()
                cleanupTempFile() // Delete temp file if it was created before the error

                // Customize error messages based on exception type
                val errorTitle = when (e) {
                    is uniffi.wp_epub_mini.EpubException.Authentication -> "Authentication Failed"
                    is uniffi.wp_epub_mini.EpubException.Download -> "Download Failed"
                    is IOException -> "File System Error"
                    else -> "An Unexpected Error Occurred"
                }

                downloadResultPanel.showResult(
                    isSuccess = false,
                    title = errorTitle,
                    message = "Reason: ${e.message}\n\nPlease check your input and network connection, then try again."
                )
                slidePane.addSlide(downloadResultPanel, SlidePaneTransition.Type.FORWARD)

            }
        }
    }
    // ----------------------------------------
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun showEmptyPassWithLoginEnabledDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Empty password with login enabled. Either enter credentials or disable login.",
                "Empty password with login enabled!",
                SimpleModalBorder.Option("Got it!", 0),
                null
            ), modelOption
        )
    }

    private fun showEmptyUsernameWithLoginEnabledDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Empty username with login enabled. Either enter credentials or disable login.",
                "Empty username with login enabled!",
                SimpleModalBorder.Option("Got it!", 0),
                null
            ), modelOption
        )
    }

    private fun showEmptyUsernameAndPasswordWithLoginEnabledDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Empty username and password with login enabled. Either enter credentials or disable login.",
                "Empty username and password with login enabled!",
                SimpleModalBorder.Option("Got it!", 0),
                null
            ), modelOption
        )
    }

    private fun doProcess(storyID: Int) {

        // This's the restore
        scope.launch {
            try {
                val story = getStoryDataAsync(storyID.toString())

                if (story != null) {
                    // 3a. Success! Populate the downloader panel with data
                    bookDownloaderPanel.storyId = storyID // If this fails the entire flow fails
                    bookDownloaderPanel.storyTitle = story.title
                    bookDownloaderPanel.authorName = story.user.name
                    bookDownloaderPanel.isMature = story.mature

                    try {
                        bytesToImage(story.coverData)?.let {
                            bookDownloaderPanel.coverImage = ImageIcon(makeRoundedCorner(resizeImage(it, 128, 200), 16))
                        }
                    } catch (e: IOException) {
                        showUnexpectedExceptionDialog(
                            this@Main, "Couldn't buffer cover image: ${e.message}"
                        )
                        slidePane.addSlide(checkoutPanel, SlidePaneTransition.Type.BACK)
                    }

                    // Switch to the populated downloader panel
                    slidePane.addSlide(bookDownloaderPanel, SlidePaneTransition.Type.FORWARD)
                } else {
                    // 3b. Story not found or API error
                    showFetchExceptionDialog(this@Main)
                    // Go back to the input panel
                    slidePane.addSlide(checkoutPanel, SlidePaneTransition.Type.BACK)
                }
            } catch (e: IOException) {
                // 3c. Network exception
                showUnexpectedExceptionDialog(this@Main, "Network request failed: ${e.message}")
                slidePane.addSlide(checkoutPanel, SlidePaneTransition.Type.BACK)
            }
        }
    }

    private fun resizeImage(original: BufferedImage, width: Int, height: Int): BufferedImage {
        // Create a new image of the desired size
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        // Get the graphics context of the new image
        val g2d = resized.createGraphics()

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw the original image onto the new image with scaling
        g2d.drawImage(original, 0, 0, width, height, null)

        // Clean up resources
        g2d.dispose()

        return resized
    }

    private fun makeRoundedCorner(image: BufferedImage, cornerRadius: Int): BufferedImage {
        val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val g2 = output.createGraphics()

        // Enable antialiasing for smooth corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Create a rounded rectangle shape to use as a "clipping mask"
        val roundRect = RoundRectangle2D.Float(
            0f, 0f, image.width.toFloat(), image.height.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat()
        )

        // Set the clip
        g2.clip = roundRect

        // Draw the original image. Only the parts within the clip will be visible.
        g2.drawImage(image, 0, 0, null)

        g2.dispose()
        return output
    }

    /**
     * Converts a ByteArray to a BufferedImage.
     * Returns null if the input bytes are null.
     * @throws IOException if the byte data is not a valid image.
     */
    @Throws(IOException::class)
    private fun bytesToImage(bytes: ByteArray?): BufferedImage? {
        if (bytes == null) {
            return null
        }
        // No try-catch here. If ImageIO.read fails, the exception is thrown
        // to the code that called this function.
        return ImageIO.read(bytes.inputStream())
    }

    private fun showSomeSeriousErrorOccurredDialog(component: Component?) {
        val message = "Some serious error occurred!"
        ModalDialog.showModal(
            component, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR, message, "Some Serious Error", SimpleModalBorder.Option("Hmm!", 0), null
            ), modelOption
        )
    }

    private fun showNoNetworkExceptionModalDialog(component: Component?) {
        ModalDialog.showModal(
            component, CustomModalDialogs(
                CustomModalDialogs.Type.WARNING,
                "No active internet connection! Couldn't access: $usingServer",
                "Couldn't access internet!",
                SimpleModalBorder.Option("I'll Check!", 0),
                null
            ), modelOption
        )
    }

    private fun showUnexpectedExceptionDialog(owner: Component?, message: String? = "Unknown Error") {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Some unexpected error occurred: $message",
                "Unexpected error!",
                SimpleModalBorder.Option("Hmm", 0),
                null
            ), modelOption
        )
    }

    private fun showFetchExceptionDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "The Story you gave us seems to be not available!",
                "Not Available!",
                SimpleModalBorder.Option("Hmm", 0),
                null
            ), modelOption
        )
    }

    private fun showNotANumberExceptionDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Your input is not a valid number despite selected type!",
                "Not a number!",
                SimpleModalBorder.Option("Ah, I'm mistaken", 0),
                null
            ), modelOption
        )
    }

    private fun showNotAValidStoryLinkExceptionDialog(owner: Component?) {
        ModalDialog.showModal(
            owner, CustomModalDialogs(
                CustomModalDialogs.Type.ERROR,
                "Your input is not a valid wattpad story link despite selected type!",
                "Not a valid story link!",
                SimpleModalBorder.Option("Ah, I'm mistaken", 0),
                null
            ), modelOption
        )
    }

    companion object {

        private val modelOption: Option?
            get() {
                return ModalDialog.createOption()
            }

        val sliderPanel: JPanel
            get() {
                val checkoutPanel = JPanel()
                checkoutPanel.setLayout(null)
                //        jPanel.setBorder(new FlatLineBorder(new Insets(4,4,4,4),9));
                checkoutPanel.setBounds(0, 0, 600 - 20, 500 - (10 + 40 + 20 + 10))
                checkoutPanel.putClientProperty(
                    FlatClientProperties.STYLE,
                    "[light]background: tint(@background,50%);" + "[dark]background: shade(@background,15%);" + "[light]border: 16,16,16,16,shade(@background,10%),,9;" + "[dark]border: 16,16,16,16,tint(@background,10%),,9"
                )
                return checkoutPanel
            }

        private val toastOption: ToastOption
            get() {
                val option = Toast.createOption()

                option.setAnimationEnabled(true).setAutoClose(true).isCloseOnClick = true

                option.style.setBackgroundType(ToastStyle.BackgroundType.DEFAULT)
                    .setShowCloseButton(false).isPaintTextColor = false

                return option
            }

        @JvmStatic
        fun main(args: Array<String>) {
            if (SystemInfo.isMacOS) {
                System.setProperty("apple.laf.useScreenMenuBar", "true")
                System.setProperty("apple.awt.application.name", "Wattpad Downloader")
                System.setProperty("apple.awt.application.appearance", "system")
            }

            FlatLaf.setUseNativeWindowDecorations(true)
            setDefaultLookAndFeelDecorated(true)
            JDialog.setDefaultLookAndFeelDecorated(true)

            UIManager.put("TitlePane.menuBarEmbedded", true)

            //        FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#095900"))

            UIManager.put(
                "Button.default.foreground", Color.white
            ); // For default buttons (often has accent background)

            UIManager.put("Button.arc", 999)
            UIManager.put("Component.arc", 999)
            UIManager.put("ProgressBar.arc", 999)
            UIManager.put("TextComponent.arc", 999)
            UIManager.put("CheckBox.arc", 999)

            //        UIManager.put( "ScrollBar.thumbArc", 999 );
            UIManager.put("ScrollBar.track", Color(0x0000000, true))
            UIManager.put("ScrollBar.width", 10)

            LafAction.setTheme(true, null)
            FlatJetBrainsMonoFont.install()

            SwingUtilities.invokeLater {
                Main(OsThemeDetector.getDetector())
            }
        }
    }
}