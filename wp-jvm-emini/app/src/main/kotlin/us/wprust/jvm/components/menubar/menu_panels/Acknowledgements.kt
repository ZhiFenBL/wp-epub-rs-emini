package us.wprust.jvm.components.menubar.menu_panels

import net.miginfocom.swing.MigLayout
import us.wprust.jvm.utils.createText
import us.wprust.jvm.utils.showUrl
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent

class Acknowledgements : JPanel() {
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
    }

    private val descriptionText: String
        get() {
            val text = "<center>\n" +
                    "        <p>With special thanks to the people who made this app possible.</p>\n" +
                    "        <br>\n" +
                    "        <hr>\n" +
                    "        <br>\n" +
                    "\n" +
                    "        <p><b>Aaron BenDaniel</b></p>\n" +
                    "        <p><i>For always helping, actively engaging in direct development, debugging, and robust testing, just like a close friend.</i></p>\n" +
                    "        <p>\n" +
                    "            <a href=\"https://github.com/aaronbendaniel\">GitHub</a> &bull;\n" +
                    "            <a href=\"https://abendaniel.top\">Website</a> &bull;\n" +
                    "            Discord: sowansow\n" +
                    "        </p>\n" +
                    "\n" +
                    "        <br>\n" +
                    "        <hr>\n" +
                    "        <br>\n" +
                    "\n" +
                    "        <p><b>Dhanush Rambhatla</b></p>\n" +
                    "        <p><i>For inspiring the EPUB version of the downloader.</i></p>\n" +
                    "        <p>\n" +
                    "            <a href=\"https://github.com/theonlywayup\">GitHub</a> &bull;\n" +
                    "            <a href=\"https://rambhat.la\">Website</a> &bull;\n" +
                    "            Discord: theonlywayup\n" +
                    "        </p>\n" +
                    "    </center>\n";

            return text
        }
}