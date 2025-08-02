// file: src/main/kotlin/us/wprust/jvm/scenes/CheckingStoryPanel.kt
package us.wprust.jvm.scenes

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatProgressBar
import us.wprust.jvm.Main.Companion.sliderPanel
import us.wprust.jvm.components.FadingLabel
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingConstants

class CheckingStoryPanel : JPanel() {

    private val downloadLabel: FlatLabel
    private val logoBook = FlatLabel()


    init {
        val basePanel = sliderPanel
        layout = basePanel.layout
        putClientProperty(FlatClientProperties.STYLE, basePanel.getClientProperty(FlatClientProperties.STYLE))

        val imageUrl = javaClass.getResource("book.gif")
        val imageBytes = imageUrl?.readBytes()
        logoBook.icon = ImageIcon(imageBytes)
        logoBook.setBounds((600 - 20 - 150) / 2, 80, 600 - 20 - 150, 150)
        add(logoBook)

        val progressBar = FlatProgressBar()
        progressBar.isIndeterminate = true
        progressBar.setBounds((600 - 20 - 175) / 2, 70 + 20 + 150, 175, 3)
        add(progressBar)

        downloadLabel = FlatLabel()
        downloadLabel.text = "We're checking for your story!"
        downloadLabel.horizontalAlignment = SwingConstants.CENTER
        downloadLabel.verticalAlignment = SwingConstants.CENTER
        downloadLabel.setBounds(0, 120, 600 - 20, 500 - (10 + 40 + 20 + 10) - 120)
        downloadLabel.labelType = FlatLabel.LabelType.h2
        add(downloadLabel)
    }

    // Public method to allow the Main class to update the status
    fun setStatus(text: String) {
        downloadLabel.text = text
    }

    override fun addNotify() {
        super.addNotify()
    }
}