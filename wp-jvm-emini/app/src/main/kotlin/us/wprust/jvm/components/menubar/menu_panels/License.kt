package us.wprust.jvm.components.menubar.menu_panels

import com.formdev.flatlaf.FlatClientProperties
import net.miginfocom.swing.MigLayout
import us.wprust.jvm.utils.createText
import us.wprust.jvm.utils.showUrl
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.event.HyperlinkEvent
import javax.swing.text.DefaultCaret

class License : JPanel() {
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
                    "        <h3>WattDownload (JVM)</h3>\n" +
                    "        <p>The best available Wattpad to EPUB Downloader App for Desktop!</p>\n" +
                    "        <br>\n" +
                    "        <hr>\n" +
                    "        <p>Copyright &copy; 2025 WattDownload</p>\n" +
                    "        <p>This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.</p>\n" +
                    "        <p>This program is distributed in the hope that it will be useful, but <b>WITHOUT ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b> or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU Affero General Public License for more details.</p>\n" +
                    "        <p>You should have received a copy of the GNU Affero General Public License along with this program. If not, see <a href=\"https://www.gnu.org/licenses/\">https://www.gnu.org/licenses/</a>.</p>\n" +
                    "    </center>";

            return text
        }
}