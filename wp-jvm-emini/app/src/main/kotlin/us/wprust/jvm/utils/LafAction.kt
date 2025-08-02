package us.wprust.jvm.utils

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import com.jthemedetecor.OsThemeDetector
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

object LafAction {
    private val Icon = ImageIcon(Objects.requireNonNull(LafAction::class.java.getResource("logo.png")))

    @JvmStatic
    fun setIcon(jFrame: JFrame?) {
        setIcon(jFrame, OsThemeDetector.getDetector().isDark)
    }

    @JvmStatic
    fun setIcon(jFrame: JFrame?, osThemeDetector: OsThemeDetector) {
        setIcon(jFrame, osThemeDetector.isDark)
    }

    @JvmStatic
    fun setIcon(jFrame: JFrame?, isDark: Boolean) {
        jFrame?.iconImage = if (isDark) Icon.getImage() else Icon.getImage()
    }

    fun setTheme(isFirstTime: Boolean, jFrame: JFrame?) {
        setTheme(OsThemeDetector.getDetector(), isFirstTime, jFrame)
    }

    fun setTheme(detector: OsThemeDetector, isFirstTime: Boolean, jFrame: JFrame?) {
        setTheme(detector.isDark, isFirstTime, jFrame)
    }

    fun setTheme(isDark: Boolean, isFirstTime: Boolean, jFrame: JFrame?) {
        setIcon(jFrame, isDark)

        val lafClassName = if (isDark) FlatMacDarkLaf::class.java.getName() else FlatMacLightLaf::class.java.getName()

        if (!isFirstTime) {
            FlatAnimatedLafChange.showSnapshot()
        }

        try {
            UIManager.setLookAndFeel(lafClassName)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        } catch (e: UnsupportedLookAndFeelException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        }

        if (!isFirstTime) {
            FlatLaf.updateUI()
            FlatLaf.revalidateAndRepaintAllFramesAndDialogs()
            FlatAnimatedLafChange.hideSnapshotWithAnimation()
        }
    }
}
