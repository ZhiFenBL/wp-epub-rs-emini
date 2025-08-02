package us.wprust.jvm.components

import com.formdev.flatlaf.extras.components.FlatLabel
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Timer

class FadingLabel : FlatLabel() {
    private var animationTimer: Timer? = null
    private var startTime: Long = 0
    private var currentAlpha = 0f // Opacity from 0.0f to 1.0f

    companion object {
        private const val FADE_DURATION = 500 // Animation duration in milliseconds
    }

    /**
     * Overrides the paintComponent method to apply transparency.
     */
    override fun paintComponent(g: Graphics) {
        // Only bother painting if there's something to see
        if (currentAlpha > 0f) {
            val g2d = g.create() as Graphics2D
            try {
                // Apply the current alpha (opacity) to the component
                g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha)
                super.paintComponent(g2d)
            } finally {
                g2d.dispose()
            }
        }
    }

    /**
     * Starts the fade-in animation.
     */
    fun fadeIn() {
        // Stop any previous animation
        animationTimer?.stop()
        startTime = System.currentTimeMillis()

        // Create and start a timer to update the alpha value
        animationTimer = Timer(20) { // Update roughly every 20ms
            val elapsed = System.currentTimeMillis() - startTime
            currentAlpha = (elapsed.toFloat() / FADE_DURATION).coerceAtMost(1.0f)
            
            if (currentAlpha >= 1.0f) {
                (it.source as Timer).stop() // Stop the timer when fade-in is complete
            }
            
            repaint() // Trigger a repaint to show the new opacity
        }.apply { start() }
    }
}