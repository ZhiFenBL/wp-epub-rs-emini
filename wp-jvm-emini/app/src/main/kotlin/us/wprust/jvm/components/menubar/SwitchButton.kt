package us.wprust.jvm.components.menubar

import us.wprust.jvm.components.EventSwitchSelected
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import javax.swing.Timer

class SwitchButton : Component() {
    fun isSelected(): Boolean {
        return selected
    }

    fun setSelected(selected: Boolean) {
        this.selected = selected
        timer.start()
        runEvent()
    }

    private val timer: Timer
    private var location: Float
    private var selected = false
    private var mouseOver = false
    private val speed = 0.2f
    private val events: MutableList<EventSwitchSelected>

    init {
        setBackground(Color(9, 89, 0))
        setPreferredSize(Dimension(50, 25))
        setForeground(Color.WHITE)
        setCursor(Cursor(Cursor.HAND_CURSOR))
        events = ArrayList<EventSwitchSelected>()
        location = 2f
        timer = Timer(0) {
            if (isSelected()) {
                val endLocation = getWidth() - getHeight() + 2
                if (location < endLocation) {
                    location += speed
                    repaint()
                } else {
                    timer.stop()
                    location = endLocation.toFloat()
                    repaint()
                }
            } else {
                val endLocation = 2
                if (location > endLocation) {
                    location -= speed
                    repaint()
                } else {
                    timer.stop()
                    location = endLocation.toFloat()
                    repaint()
                }
            }
        }
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(me: MouseEvent?) {
                mouseOver = true
            }

            override fun mouseExited(me: MouseEvent?) {
                mouseOver = false
            }

            override fun mouseReleased(me: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    if (mouseOver) {
                        selected = !selected
                        timer.start()
                        runEvent()
                    }
                }
            }
        })
    }

    override fun paint(grphcs: Graphics?) {
        val g2 = grphcs as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val width = getWidth()
        val height = getHeight()

        // --- THE FIX IS HERE ---
        // If no animation is running, snap the slider's location to match the
        // actual 'selected' state. This corrects the visual appearance before painting.
        if (!timer.isRunning) {
            location = if (selected) (width - height + 2).toFloat() else 2f
        }

        // The rest of your painting logic remains exactly the same
        val alpha = this.alpha
        if (alpha < 1) {
            g2.color = Color.GRAY
            g2.fillRoundRect(0, 0, width, height, 25, 25)
        }
        g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2.color = getBackground()
        g2.fillRoundRect(0, 0, width, height, 25, 25)
        g2.color = getForeground()
        g2.composite = AlphaComposite.SrcOver
        g2.fillOval(location.toInt(), 2, height - 4, height - 4)
        super.paint(grphcs)
    }

    private val alpha: Float
        get() {
            val width = (getWidth() - getHeight()).toFloat()
            var alpha: Float = (location - 2) / width
            if (alpha < 0) {
                alpha = 0f
            }
            if (alpha > 1) {
                alpha = 1f
            }
            return alpha
        }

    private fun runEvent() {
        for (event in events) {
            event.onSelected(selected)
        }
    }

    fun addEventSelected(event: EventSwitchSelected?) {
        events.add(event!!)
    }
}