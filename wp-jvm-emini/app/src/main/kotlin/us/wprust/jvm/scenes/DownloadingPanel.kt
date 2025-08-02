// src/main/kotlin/us/wprust/jvm/scenes/DownloadingPanel.kt
package us.wprust.jvm.scenes

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatProgressBar
import us.wprust.jvm.Main.Companion.sliderPanel
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JPanel
import javax.swing.SwingConstants

class DownloadingPanel : JPanel() {

    private val statusLabel = FlatLabel()
    private val timerLabel = FlatLabel()
    private val progressBar = FlatProgressBar()

    init {
        val basePanel = sliderPanel
        layout = basePanel.layout
        putClientProperty(FlatClientProperties.STYLE, basePanel.getClientProperty(FlatClientProperties.STYLE))

        val progressBar = FlatProgressBar()
        progressBar.isIndeterminate = true
        progressBar.setBounds((600 - 20 - 175) / 2, 70 + 20 + 150, 175, 3)
        add(progressBar)

        val logoBook = FlatLabel()
        val imageUrl = javaClass.getResource("waiting.gif")
        val imageBytes = imageUrl?.readBytes()
        logoBook.icon = ImageIcon(imageBytes)
        logoBook.setBounds((600 - 20 - 150) / 2, 80, 600 - 20 - 150, 150)
        add(logoBook)

        statusLabel.text = "Initializing Download!"
        statusLabel.horizontalAlignment = SwingConstants.CENTER
        statusLabel.verticalAlignment = SwingConstants.CENTER
        statusLabel.setBounds(0, 120, 600 - 20, 500 - (10 + 40 + 20 + 10) - 120)
        statusLabel.labelType = FlatLabel.LabelType.h2
        add(statusLabel)

        timerLabel.text = "00:00.000"
        timerLabel.horizontalAlignment = SwingConstants.CENTER
        timerLabel.setBounds(0, 120 + 150 + 20, 600 - 20, 25)
        timerLabel.labelType = FlatLabel.LabelType.h4
        add(timerLabel)

        progressBar.isIndeterminate = true
        progressBar.preferredSize = Dimension(200, 20)
        progressBar.alignmentX = CENTER_ALIGNMENT
    }

    fun updateTimer(text: String) {
        timerLabel.text = text
    }

    fun updateStatus(text: String) {
        statusLabel.text = text
    }
}