package us.wprust.jvm.scenes

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatPasswordField
import com.formdev.flatlaf.extras.components.FlatTextField
import us.wprust.jvm.Main
import us.wprust.jvm.components.LabeledSwitch
import java.awt.*
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class BookDownloaderPanel : JPanel() {

    // Private UI components
    private val storyTitleLabel: FlatLabel
    private val coverImageLabel: JLabel
    private val matureLabel: FlatLabel
    private val notMatureLabel: FlatLabel
    private val authorNameLabel: FlatLabel
    private val downloadButton: FlatButton
    private val isLoginSwitch: LabeledSwitch
    private val usernameField: FlatTextField
    private val passwordField: FlatPasswordField
    private val isDownloadImages: LabeledSwitch

    // Public callback for the download event
    lateinit var onDownload: (username: String?, password: String?) -> Unit

    init {
        // Use the base panel configuration from Main
        val basePanel = Main.sliderPanel
        layout = basePanel.layout
        putClientProperty(FlatClientProperties.STYLE, basePanel.getClientProperty(FlatClientProperties.STYLE))

        // --- Create and add all components ---

        storyTitleLabel = FlatLabel().apply {
            text = "Loading story title..."
            horizontalAlignment = SwingConstants.CENTER
            setBounds(50 + 128 + 20, 50, 580 - (50 + 128 + 20) - 50, 30)
            labelType = FlatLabel.LabelType.h2
        }
        add(storyTitleLabel)

        isDownloadImages = LabeledSwitch("Download Images").apply {
            alignmentY = CENTER_ALIGNMENT
            setBounds((580 + (30 + 128) - 175) / 2, 100, 175, 25)
        }
        isDownloadImages.isSelected = true
        add(isDownloadImages)

        // --- Login Panel Setup ---
        isLoginSwitch = LabeledSwitch("Login").apply {
            alignmentY = CENTER_ALIGNMENT
        }
        usernameField = FlatTextField().apply {
            placeholderText = "Login is disabled"
            isEnabled = false
            preferredSize = Dimension(200, 30)
        }
        passwordField = FlatPasswordField().apply {
            placeholderText = "Login is disabled"
            isEnabled = false
            putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true")
            preferredSize = Dimension(200, 30)
        }

        val loginPanel = JPanel()
        // 2. Create a BoxLayout that manages the 'loginPanel' itself.
        loginPanel.layout = GridBagLayout()
        loginPanel.putClientProperty(
            FlatClientProperties.STYLE,
            "[light]background: tint(@background,50%);" + "[dark]background: shade(@background,15%);" + "[light]border: 16,16,16,16,shade(@background,10%),,9;" + "[dark]border: 16,16,16,16,tint(@background,10%),,9"
        )

        val gbc = GridBagConstraints()
        gbc.gridwidth = GridBagConstraints.REMAINDER
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(5, 0, 5, 0)

        // 3. Configure the panel and add its components.
        loginPanel.apply {
//            isOpaque = false // Make it transparent
            add(isLoginSwitch, gbc)
            add(usernameField, gbc)
            add(passwordField, gbc)
            // Position the entire login group
            setBounds((580 - 220) / 2 + 70 - 15, 100 + 25 + 20, 250, 150)
        }
        add(loginPanel)

        coverImageLabel = JLabel().apply {
            setBounds(50, 50, 128, 200)
            isOpaque = true
        }

        coverImageLabel.putClientProperty(
            FlatClientProperties.STYLE,
            "[light]background: tint(@background,50%);" +
                    "[dark]background: shade(@background,15%);" +
                    "[light]border: 0,0,0,0,shade(@background,10%),,16;" + // Insets are now 0
                    "[dark]border: 0,0,0,0,tint(@background,10%),,16"   // Insets are now 0
        )

        add(coverImageLabel)

        matureLabel = FlatLabel().apply {
            text = "Mature"
            isVisible = false
            horizontalAlignment = SwingConstants.CENTER
            putClientProperty(FlatClientProperties.STYLE, "arc: 999; border: 2,10,2,10;")
            background = Color(115, 10, 10)
            isOpaque = true // Required for background color to show
            foreground = Color.white
            setBounds(50 + 25, 50 + 200 + 10, 128 - 50, 20)
        }
        add(matureLabel)

        notMatureLabel = FlatLabel().apply {
            text = "Not Mature"
            isVisible = true
            horizontalAlignment = SwingConstants.CENTER
            putClientProperty(FlatClientProperties.STYLE, "arc: 999; border: 2,10,2,10;")
            background = Color(9, 89, 0)
            isOpaque = true // Required for background color to show
            foreground = Color.white
            setBounds(50 + 20, 50 + 200 + 10, 128 - 40, 20)
        }
        add(notMatureLabel)

        authorNameLabel = FlatLabel().apply {
            text = "by loading..."
            horizontalAlignment = SwingConstants.CENTER
            setBounds(50, 50 + 200 + 10 + 25, 128, 20)
        }
        add(authorNameLabel)

        downloadButton = FlatButton().apply {
            text = "Download"
            setBounds(100, 50 + 200 + 10 + 25 + 20 + 40, 580 - 200, 30)
        }
        add(downloadButton)

        // --- Internal Event Handling ---

        isLoginSwitch.addEventSelected { selected ->
            usernameField.isEnabled = selected
            passwordField.isEnabled = selected
            if (selected) {
                usernameField.requestFocus()
                usernameField.placeholderText = "Enter Wattpad Username"
                passwordField.placeholderText = "Enter Wattpad Password"
            } else {
                usernameField.text = null
                usernameField.placeholderText = "Login is disabled"
                passwordField.text = null
                passwordField.placeholderText = "Login is disabled"
            }
        }

        downloadButton.addActionListener {
            if (::onDownload.isInitialized) {
                val user = if (isLoginSwitch.isSelected) usernameField.text else null
                val pass = if (isLoginSwitch.isSelected) String(passwordField.password) else null
                onDownload(user, pass)
            }
        }
    }

    // --- Public API to configure the component ---

    var storyId: Int = 0

    val isLogin: Boolean
        get() = isLoginSwitch.isSelected

    val isEmbedImages: Boolean
        get() = isDownloadImages.isSelected

    var storyTitle: String
        get() = storyTitleLabel.text
        set(value) {
            storyTitleLabel.text = value
        }

    var authorName: String
        get() = authorNameLabel.text
        set(value) {
            authorNameLabel.text = "by $value"
        }

    var coverImage: Icon?
        get() = coverImageLabel.icon
        set(value) {
            coverImageLabel.icon = value
        }

    var isMature: Boolean
        get() = matureLabel.isVisible
        set(value) {
            matureLabel.isVisible = value
            notMatureLabel.isVisible = !value
        }

    override fun addNotify() {
        super.addNotify()
        // Set the default button when this panel is shown
        rootPane.defaultButton = downloadButton
    }
}