package us.wprust.jvm.components.menubar

import us.wprust.jvm.components.EventSwitchSelected
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel

class LabeledSwitch(text: String?) : JPanel() {
    private val label: JLabel = JLabel(text)
    private val switchButton: SwitchButton = SwitchButton()

    /**
     * Creates a new LabeledSwitch component.
     * @param text The text to display next to the switch.
     */
    init {

        // --- CHANGE HERE ---
        // Use FlowLayout for left-aligned components with a 10px horizontal gap.
        // FlowLayout(alignment, horizontal_gap, vertical_gap)
        setLayout(FlowLayout(FlowLayout.LEFT, 10, 0))

        // Make the panel transparent to not interfere with parent backgrounds
        setOpaque(false)

        // --- CHANGE HERE ---
        // Add components without constraints. The layout manager handles positioning.
        add(label)
        add(switchButton)
    }

    var isSelected: Boolean
        /**
         * Checks if the switch is in the "selected" or "on" state.
         * @return true if selected, false otherwise.
         */
        get() = switchButton.isSelected()
        /**
         * Programmatically sets the state of the switch.
         * @param selected The new state.
         */
        set(selected) {
            switchButton.setSelected(selected)
        }

    /**
     * Adds an event listener that fires when the switch is toggled.
     * @param event The event to add.
     */
    fun addEventSelected(event: EventSwitchSelected?) {
        switchButton.addEventSelected(event)
    }

    var text: String?
        /**
         * Gets the current text of the label.
         * @return The label's text.
         */
        get() = label.text
        /**
         * Sets the text of the label.
         * @param text The new text for the label.
         */
        set(text) {
            label.setText(text)
        }
}