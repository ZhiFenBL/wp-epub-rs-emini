// src/main/kotlin/us/wprust/jvm/scenes/DownloadResultPanel.kt
package us.wprust.jvm.scenes

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import us.wprust.jvm.Main.Companion.sliderPanel
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingConstants

class DownloadResultPanel : JPanel() {

    private val iconLabel = FlatLabel()
    private val titleLabel = FlatLabel()
    private val messageArea = FlatLabel()
    private val saveButton = FlatButton()
    private val restartButton = FlatButton()

    // Callbacks to be set by the Main class
    var onSave: (() -> Unit)? = null
    var onRestart: (() -> Unit)? = null

    init {
        val basePanel = sliderPanel
        layout = basePanel.layout
        putClientProperty(FlatClientProperties.STYLE, basePanel.getClientProperty(FlatClientProperties.STYLE))

//        saveButton.preferredSize = Dimension(200, 30)
//        restartButton.preferredSize = Dimension(200, 30)

        iconLabel.setBounds((600 - 20 - 150) / 2, 80, 600 - 20 - 150, 150)
        iconLabel.isOpaque = false
        add(iconLabel)

        titleLabel.text = "Status Title!"
        titleLabel.horizontalAlignment = SwingConstants.CENTER
        titleLabel.verticalAlignment = SwingConstants.CENTER
        titleLabel.setBounds(0, 120, 600 - 20, 500 - (10 + 40 + 20 + 10) - 120)
        titleLabel.labelType = FlatLabel.LabelType.h2
        add(titleLabel)

        // Center panel for the detailed message
        messageArea.text = "Details about the result will appear here."
        messageArea.horizontalAlignment = SwingConstants.CENTER
        messageArea.setBounds(0, 120 + 150 + 20, 600 - 20, 25)
        messageArea.labelType = FlatLabel.LabelType.h4
        add(messageArea)

        // Bottom panel for buttons
        val buttonPanel = JPanel()
        buttonPanel.isOpaque = false
        buttonPanel.add(saveButton)
        buttonPanel.add(restartButton)
        buttonPanel.setBounds(0, 120 + 150 + 20 + 25 + 20, 600 - 20, 45)
        add(buttonPanel)

        // Button actions
        saveButton.addActionListener { onSave?.invoke() }
        restartButton.addActionListener { onRestart?.invoke() }

        showResult(true, "title here", "Message here")
    }

    fun showResult(isSuccess: Boolean, title: String, message: String) {
        titleLabel.text = title
        messageArea.text = message
        saveButton.text = "Save As..."
        saveButton.isVisible = isSuccess
        restartButton.text = "Download Another Story"

        // --- MODIFIED SECTION ---
        val imageBytes = if (isSuccess) {
            javaClass.getResource("success.gif")?.readBytes()
        } else {
            javaClass.getResource("error.gif")?.readBytes()
        }

        iconLabel.icon = ImageIcon(imageBytes)
        // --- END MODIFIED SECTION ---
    }

    /**
     * This method is called by Swing when the panel is added to a window.
     * It's the safe place to set the default button.
     */
    override fun addNotify() {
        super.addNotify() // Always call the super method first
        rootPane.defaultButton = restartButton
    }
}