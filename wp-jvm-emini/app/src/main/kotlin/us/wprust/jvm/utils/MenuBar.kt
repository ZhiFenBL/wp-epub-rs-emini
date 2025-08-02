package us.wprust.jvm.utils

import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.IOException
import java.net.URI
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JSeparator
import kotlin.system.exitProcess

object MenuBar {
    fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        val fileMenu = JMenu("File")
        fileMenu.setMnemonic('F')
        menuBar.add(fileMenu)

        val exitItem = JMenuItem("Exit")
        exitItem.setMnemonic('X')
        exitItem.addActionListener { _: ActionEvent? -> exitProcess(0) }
        fileMenu.add(exitItem)

        val helpMenu = JMenu("Help")
        helpMenu.setMnemonic('H')
        menuBar.add(helpMenu)

        val aboutItem = JMenuItem("About")
        aboutItem.setMnemonic('A')
        aboutItem.addActionListener { _: ActionEvent? ->
            try {
                Desktop.getDesktop().browse(URI.create("https://wpdl.us/application/singledl/desktop/java/help"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        helpMenu.add(aboutItem)

        val separator = JSeparator()
        helpMenu.add(separator)

        val licenseItem = JMenuItem("License")
        licenseItem.setMnemonic('L')
        licenseItem.addActionListener { _: ActionEvent? ->
            try {
                Desktop.getDesktop()
                    .browse(URI.create("https://wpdl.us/application/singledl/desktop/java/help/license"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        helpMenu.add(licenseItem)

        val privacyItem = JMenuItem("Privacy Policy")
        privacyItem.setMnemonic('P')
        privacyItem.addActionListener { _: ActionEvent? ->
            try {
                Desktop.getDesktop()
                    .browse(URI.create("https://wpdl.us/application/singledl/desktop/java/help/privacy-policy"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        helpMenu.add(privacyItem)

        val termsItem = JMenuItem("Terms of Service")
        termsItem.setMnemonic('T')
        termsItem.addActionListener { _: ActionEvent? ->
            try {
                Desktop.getDesktop()
                    .browse(URI.create("https://wpdl.us/application/singledl/desktop/java/help/terms-of-service"))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        helpMenu.add(termsItem)

        return menuBar
    }
}
