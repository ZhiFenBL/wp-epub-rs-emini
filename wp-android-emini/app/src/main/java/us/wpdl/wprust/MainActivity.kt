package us.wpdl.wprust

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import us.wpdl.wprust.ConnectionUtil.hasInternetAccess
import us.wpdl.wprust.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val downloadActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if the activity returned a success code
        if (result.resultCode == RESULT_OK) {
            // This code runs when DownloadActivity sends back a successful result.
            // Clear the text fields here.
            binding.urlBox.text?.clear()
            binding.usernameBox.text?.clear()
            binding.passwordBox.text?.clear()

            if (binding.expandableCredentialsLayout.isVisible) {
                // Add a smooth animation for the change
                TransitionManager.beginDelayedTransition(binding.rootContainer)
                // It's visible, so collapse it
                binding.expandableCredentialsLayout.visibility = GONE
                // Rotate the arrow icon back to its original state
                binding.expansionArrowIcon.rotation = 0f
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        setupExpansionPanel()
        binding.checkoutBtn.setOnClickListener {
            if (hasInternetAccess(applicationContext)) {
                // Get the text from your input field
                val url = binding.urlBox.text.toString().trim()
                val userBox = binding.usernameBox.text.toString().trim()
                val passBox = binding.passwordBox.text.toString().trim()
                val imgDl = binding.isImages.isChecked

                // Use the new function to validate and normalize the URL
                val normalizedUrl = normalizeWattpadUrl(url)

                if (normalizedUrl != null) {
                    // Success! The input was valid and is now in a standard format.
                    binding.textInputLayout.error = null // Clear any previous error message

                    val intent = Intent(this, DownloadActivity::class.java).apply {
                        putExtra("STORY_URL", normalizedUrl).putExtra("ISIMAGES", imgDl)
                    }

                    // Do other checks
                    binding.usernameLayout.error = null
                    binding.passwordLayout.error = null

                    when {
                        // Case 1: Username is filled but password is not
                        userBox.isNotEmpty() && passBox.isEmpty() -> {
                            binding.passwordLayout.error = "Please enter password"
                        }

                        // Case 2: Password is filled but username is not
                        passBox.isNotEmpty() && userBox.isEmpty() -> {
                            binding.usernameLayout.error = "Please enter username"
                        }

                        // Case 3: Both are filled OR both are empty (valid states)
                        else -> {
                            // Only add extras if the fields are actually filled
                            if (userBox.isNotEmpty()) {
                                intent.apply {
                                    putExtra("USERNAME", userBox)
                                    putExtra("PASSWORD", passBox)
                                }
                            }

                            downloadActivityLauncher.launch(intent)
                        }
                    }
                } else {
                    // The input didn't match any valid Wattpad format.
                    binding.textInputLayout.error = "Please enter a valid Wattpad URL or Story ID"
                }
            } else {
                doNoInternet()
            }
        }
    }

    private fun setupExpansionPanel() {
//         The header area that the user clicks
        binding.expansionHeaderClickable.setOnClickListener {

            // Add a smooth animation for the change
            TransitionManager.beginDelayedTransition(binding.rootContainer)

            // Check the current visibility of the content
            if (binding.expandableCredentialsLayout.isVisible) {
                // It's visible, so collapse it
                binding.expandableCredentialsLayout.visibility = GONE
                // Rotate the arrow icon back to its original state (pointing down)
                binding.expansionArrowIcon.rotation = 0f
            } else {
                // It's hidden, so expand it
                binding.expandableCredentialsLayout.visibility = VISIBLE
                // Rotate the arrow icon to point up
                binding.expansionArrowIcon.rotation = 180f
            }
        }
    }

    /**
     * Validates and normalizes various Wattpad URL/ID formats into a standard URL.
     * @param input The user's input string.
     * @return A standardized Wattpad story URL if the input is valid, otherwise null.
     */
    fun normalizeWattpadUrl(input: String?): String? {
        val trimmedInput = input?.trim() ?: return null

        // Regex for patterns like "wattpad.com/story/12345" or just "story/12345"
        val fullPattern = Regex("""(?:wattpad\.com/story/|story/)(\d+)""")
        var storyId = fullPattern.find(trimmedInput)?.groupValues?.getOrNull(1)
        if (storyId != null) {
            return "https://www.wattpad.com/story/$storyId"
        }

        // Regex for patterns like "wattpad.com/12345"
        val simplePattern = Regex("""wattpad\.com/(\d+)""")
        storyId = simplePattern.find(trimmedInput)?.groupValues?.getOrNull(1)
        if (storyId != null) {
            return "https://www.wattpad.com/story/$storyId"
        }

        // Regex for inputs that are just the story ID (e.g., "12345")
        val idOnlyPattern = Regex("""^\d+$""")
        if (idOnlyPattern.matches(trimmedInput)) {
            storyId = trimmedInput
            return "https://www.wattpad.com/story/$storyId"
        }

        // If no pattern matched, the input is invalid
        return null
    }

    private fun doNoInternet() {
        val inflater = LayoutInflater.from(this)
        val noInternetView = inflater.inflate(R.layout.nointernet_dialog, null)

        val noInternetDialog: AlertDialog = MaterialAlertDialogBuilder(this).setView(noInternetView)
            .setPositiveButton("Got it!") { dialog, _ -> dialog.dismiss() }.setCancelable(false)
            .create()

        noInternetDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 1. Inflate your custom layout
        val dialogAboutView = layoutInflater.inflate(R.layout.dialog_acknowledgements, null)

        // 2. Find the TextView inside your custom layout
        val aboutTextView = dialogAboutView.findViewById<MaterialTextView>(R.id.acknowledgements_text)

        // 3. Prepare your formatted text from HTML
        val formattedTextAbout = HtmlCompat.fromHtml(
            getString(R.string.about_app_html),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // 4. Set the text and the MovementMethod on your TextView
        aboutTextView.text = formattedTextAbout
        aboutTextView.movementMethod = LinkMovementMethod.getInstance() // This makes links clickable!

        ////////////////////////////////////////////////////////////////////////////////////////////

        // 1. Inflate your custom layout
        val dialogAcknoView = layoutInflater.inflate(R.layout.dialog_acknowledgements, null)

        // 2. Find the TextView inside your custom layout
        val acknoTextView = dialogAcknoView.findViewById<MaterialTextView>(R.id.acknowledgements_text)

        // 3. Prepare your formatted text from HTML
        val formattedTextAcknos = HtmlCompat.fromHtml(
            getString(R.string.acknowledgements_html),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        // 4. Set the text and the MovementMethod on your TextView
        acknoTextView.text = formattedTextAcknos
        acknoTextView.movementMethod = LinkMovementMethod.getInstance() // This makes links clickable!

        // Handle menu item clicks
        return when (item.itemId) {
            R.id.action_about -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("About WPRust")
                    .setView(dialogAboutView) // Use setView() instead of setMessage()
                    .setPositiveButton("Satisfied!") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(true)
                    .show()
                true
            }

            R.id.action_tnx ->{
                MaterialAlertDialogBuilder(this)
                    .setTitle("Acknowledgements")
                    .setView(dialogAcknoView) // This will now render the centered content correctly
                    .setPositiveButton("Grateful!") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(true)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}