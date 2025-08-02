package us.wprust.jvm.components.menubar

import raven.modal.ModalDialog
import raven.modal.component.SimpleModalBorder
import us.wprust.jvm.components.menubar.menu_panels.About
import us.wprust.jvm.components.menubar.menu_panels.Acknowledgements
import us.wprust.jvm.components.menubar.menu_panels.License
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.IOException
import java.net.URI
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JSeparator
import kotlin.system.exitProcess

object MenuBar {
    fun createMenuBar(frame: JFrame): JMenuBar {
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
            ModalDialog.showModal(
                frame, SimpleModalBorder(About(), "About"),
                ModalDialog.createOption().setAnimationEnabled(true)
            )
        }
        helpMenu.add(aboutItem)

        val separator = JSeparator()
        helpMenu.add(separator)

        val licenseItem = JMenuItem("License")
        licenseItem.setMnemonic('L')
        licenseItem.addActionListener { _: ActionEvent? ->
            ModalDialog.showModal(
                frame, SimpleModalBorder(License(), "License"),
                ModalDialog.createOption().setAnimationEnabled(true)
            )
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
//        helpMenu.add(privacyItem)

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
//        helpMenu.add(termsItem)

        val acknowledgementsItem = JMenuItem("Acknowledgements")
        acknowledgementsItem.setMnemonic('K')
        acknowledgementsItem.addActionListener { _: ActionEvent? ->
            ModalDialog.showModal(
                frame, SimpleModalBorder(Acknowledgements(), "Acknowledgements"),
                ModalDialog.createOption().setAnimationEnabled(true)
            )
        }
        helpMenu.add(acknowledgementsItem)

        return menuBar
    }
}