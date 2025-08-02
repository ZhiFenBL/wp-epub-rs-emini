package us.wprust.jvm.utils

import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JTextPane
import javax.swing.text.DefaultCaret

fun createText(text: String?): JTextPane {
    val textPane = JTextPane()
    textPane.setBorder(BorderFactory.createEmptyBorder())
    textPane.text = text
    textPane.isEditable = false
    textPane.setCaret(object : DefaultCaret() {
        override fun paint(g: Graphics?) {
        }
    })
    return textPane
}