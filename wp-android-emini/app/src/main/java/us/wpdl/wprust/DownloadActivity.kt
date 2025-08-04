package us.wpdl.wprust

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.wp_epub_mini.downloadWattpadStory
import us.wpdl.wprust.databinding.ActivityDownloadBinding
import java.io.File
import java.io.IOException

class DownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadBinding

    private var tempEpubUri: Uri? = null
    private val KEY_TEMP_URI = "temp_epub_uri"

    private var timerJob: Job? = null
    private var downloadStartTime: Long = 0L

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/epub+zip")
    ) { destinationUri: Uri? ->
        if (destinationUri != null) {
            // We have a destination. Check if our temporary source file exists.
            tempEpubUri?.let { sourceUri ->
                copyFile(sourceUri, destinationUri)
            } ?: run {
                showError("Could not find the downloaded file to save. Please try again.")
            }
        } else {
            Snackbar.make(binding.root, "Save operation cancelled.", Snackbar.LENGTH_SHORT).show()
            // If cancelled, clean up the temp file
            cleanupTempFile()
        }
    }

    private fun formatElapsedTime(millis: Long): String {
        val minutes = (millis / (1000 * 60)) % 60
        val seconds = (millis / 1000) % 60
        val remainingMillis = millis % 1000
        // Format to include minutes, seconds, and three digits for milliseconds
        return String.format("%02d:%02d.%03d", minutes, seconds, remainingMillis)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the URI of our temporary file if it exists
        tempEpubUri?.let {
            outState.putString(KEY_TEMP_URI, it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore the temp file URI if the activity is being recreated
        if (savedInstanceState != null) {
            savedInstanceState.getString(KEY_TEMP_URI)?.let { uriString ->
                tempEpubUri = Uri.parse(uriString)
            }
        }

        binding.finishButton.setOnClickListener {
            setResult(RESULT_OK) // Signal to MainActivity that we are done
            finish()
        }

        // Only start a new download if we don't have a temp file waiting to be saved.
        if (tempEpubUri == null) {
            val storyUrl = intent.getStringExtra("STORY_URL") ?: run {
                showError("No URL was provided.")
                return
            }
            val storyId = parseStoryIdFromUrl(storyUrl) ?: run {
                showError("Could not find a valid story ID in the URL.")
                return
            }
            val isUseImages = intent.getBooleanExtra("ISIMAGES", true)

            // MODIFIED: Get username and password from the intent
            val username = intent.getStringExtra("USERNAME")
            val password = intent.getStringExtra("PASSWORD")

            // MODIFIED: Pass credentials to the download process
            startDownloadProcess(storyId, isUseImages, username, password)
        } else {
            // The file is already downloaded, just prompt the user to save it.
            promptUserToSaveFile("story.epub")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun startDownloadProcess(
        storyId: Int,
        isImages: Boolean,
        username: String?,
        password: String?
    ) {
        downloadStartTime = System.currentTimeMillis() // Record start time

        timerJob = lifecycleScope.launch(Dispatchers.Main) {
            binding.timerTextView.visibility = View.VISIBLE
            while (true) {
                val elapsedMillis = System.currentTimeMillis() - downloadStartTime
                binding.timerTextView.text = formatElapsedTime(elapsedMillis)
                // Update the UI more frequently for a smooth millisecond display
                delay(67)
            }
        }


        binding.statusTextView.text = "Starting download..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                // Check for credentials and call the authentication method.
                if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {

                    uniffi.wp_epub_mini.login(username, password)

                    withContext(Dispatchers.Main) {
                        binding.statusTextView.text =
                            "Authentication successful. Starting download..."
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.statusTextView.text = "Starting download..."
                    }
                }

                // 1. Create a path in the app's cache for Rust to write to
                val tempFile = File(cacheDir, "temp_story.epub")
                val tempFilePath = tempFile.absolutePath

                // 2. Call Rust and pass the file path
                val result = downloadWattpadStory(
                    storyId = storyId,
                    embedImages = isImages,
                    concurrentRequests = 20uL,
                    outputPath = tempFilePath // <-- Pass the path here
                )

                // The download is finished. Now stop the timer.
                val durationMillis = System.currentTimeMillis() - downloadStartTime
                val formattedTime = formatElapsedTime(durationMillis)

                // 3. Store the URI of the successfully created temp file
                tempEpubUri = Uri.fromFile(File(result.outputPath))

                // 4. Switch to the main thread to prompt the user
                // Switch to the main thread to update UI and prompt the user
                withContext(Dispatchers.Main) {
                    timerJob?.cancel() // Stop the timer
                    binding.timerTextView.text = formattedTime // Set the final time

                    // Update the status text to show completion time
                    binding.statusTextView.text = "Download complete in $formattedTime. Please choose a save location."

                    // Now prompt to save
                    val defaultFileName = sanitizeFilename(result.title) + ".epub"
                    promptUserToSaveFile(defaultFileName)
                }
            } catch (e: uniffi.wp_epub_mini.EpubException.Authentication) {
                println("❌ Login failed!")
                println("   └── Reason: ${e.reason}")
                withContext(Dispatchers.Main) {
                    showError("A auth error occurred: ${e.message}")
                }
            } catch (e: uniffi.wp_epub_mini.EpubException.Download) {
                println("❌ Download failed: ${e.reason}")
                withContext(Dispatchers.Main) {
                    showError("A download error occurred: ${e.message}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("An unexpected error occurred: ${e.message}")
                }
            }
        }
    }

    private fun promptUserToSaveFile(defaultFileName: String) {
        binding.statusTextView.text = "Download complete. Please choose a save location."
        filePickerLauncher.launch(defaultFileName)
    }

    private fun copyFile(sourceUri: Uri, destinationUri: Uri) {
        lifecycleScope.launch {
            try {
                // Perform copy operation on a background thread
                withContext(Dispatchers.IO) {
                    // Use contentResolver to open streams for both URIs
                    contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                            // Efficiently copy the data
                            inputStream.copyTo(outputStream)
                        } ?: throw IOException("Could not open destination file.")
                    } ?: throw IOException("Could not open temporary source file.")
                }

                // UI updates on the main thread
                showSuccess("Download complete!")
                Snackbar.make(binding.root, "File saved successfully!", Snackbar.LENGTH_LONG)
                    .setAction("OPEN") {
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(destinationUri, "application/epub+zip")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            startActivity(viewIntent)
                        } catch (e: ActivityNotFoundException) {
                            Snackbar.make(
                                binding.root,
                                "No application found to open EPUB files.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .show()

            } catch (e: IOException) {
                showError("Failed to save the file: ${e.localizedMessage}")
            } finally {
                // IMPORTANT: Clean up the temporary file after the operation
                cleanupTempFile()
            }
        }
    }

    private fun cleanupTempFile() {
        // Delete the file from the cache directory
        tempEpubUri?.path?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
        tempEpubUri = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure the temp file is deleted if the user navigates away
        timerJob?.cancel() // Explicitly cancel the job
        if (isFinishing) {
            cleanupTempFile()
        }
    }


    // --- Helper and UI Functions ---

    /**
     * Parses the numeric ID from a standardized Wattpad story URL.
     * Example: "https://www.wattpad.com/story/12345" -> 12345
     */
    private fun parseStoryIdFromUrl(url: String): Int? {
        return url.substringAfterLast('/').split('-').firstOrNull()?.toIntOrNull()
    }

    private fun sanitizeFilename(name: String): String {
        val invalidChars = setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')
        return name.map { if (it in invalidChars) '_' else it }.joinToString("")
    }

    private fun showSuccess(message: String) {
        // The timer is already stopped. We just update the final state.
        setResult(RESULT_OK)
        binding.progressIndicator.visibility = View.GONE

        // Use the message passed from copyFile
        binding.statusTextView.text = message

        binding.finishButton.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        timerJob?.cancel() // Stop the timer on error too

        binding.progressIndicator.visibility = View.GONE
        binding.statusTextView.text = "Error: $message"
        binding.finishButton.visibility = View.VISIBLE

        MaterialAlertDialogBuilder(this)
            .setTitle("An Error Occurred")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
