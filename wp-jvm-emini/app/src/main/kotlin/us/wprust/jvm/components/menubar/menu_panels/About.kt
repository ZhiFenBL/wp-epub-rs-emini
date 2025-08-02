package us.wprust.jvm.components.menubar.menu_panels

import com.formdev.flatlaf.FlatClientProperties
import net.miginfocom.swing.MigLayout
import us.wprust.jvm.utils.createText
import us.wprust.jvm.utils.showUrl
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.border.TitledBorder
import javax.swing.event.HyperlinkEvent
import javax.swing.text.DefaultCaret

class About : JPanel() {
    init {
        init()
    }

    private fun init() {
        setLayout(MigLayout("fillx,wrap,insets 5 30 5 30,width 400", "[fill,330::]", ""))

        val description = createText("")
        description.setContentType("text/html")
        description.text = this.descriptionText
        description.addHyperlinkListener { e: HyperlinkEvent? ->
            if (e!!.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                showUrl(e.url)
            }
        }

        add(description)
        add(createSystemInformation())
    }

    private val descriptionText: String
        get() {
            val text = "WattDownload (JVM) is the best available application to download WP stories, " +
                    "built with rust & kotlin.<br>" +
                    "For source code, visit the <a href=\"https://github.com/WattDownload/wp-epub-rs-emini\">GitHub Repo.</a>"

            return text
        }

    private val systemInformationText: String
        get() {
            val text = "<b>WattDownloader Version: </b>%s<br/>" +
                    "<b>Java: </b>%s<br/>" +
                    "<b>System: </b>%s<br/>"

            return text
        }

    private fun createSystemInformation(): JComponent {
        val panel = JPanel(MigLayout("wrap"))
        panel.setBorder(TitledBorder("System Information"))
        val textPane = createText("")
        textPane.setContentType("text/html")
        val version: String? = "0.1.0"
        val java = System.getProperty("java.vendor") + " - v" + System.getProperty("java.version")
        val system: String =
            System.getProperty("os.name") + " " + System.getProperty("os.arch") + " - v" + System.getProperty(
                "os.version"
            )
        val text = String.format(
            this.systemInformationText,
            version,
            java,
            system
        )
        textPane.text = text
        panel.add(textPane)
        return panel
    }
}