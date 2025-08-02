package us.wprust.jvm.utils

import com.formdev.flatlaf.util.LoggingFacade
import java.awt.Desktop
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

fun showUrl(url: URL) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI())
            } catch (e: IOException) {
                LoggingFacade.INSTANCE.logSevere("Error browse url", e)
            } catch (e: URISyntaxException) {
                LoggingFacade.INSTANCE.logSevere("Error browse url", e)
            }
        }
    }
}