package us.wprust.jvm.utils

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import net.miginfocom.swing.MigLayout
import raven.modal.Toast
import raven.modal.component.SimpleModalBorder
import raven.modal.listener.ModalCallback
import raven.modal.toast.ToastPanel.ThemesData
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.*
import javax.swing.text.DefaultCaret

@Suppress("unused")
class CustomModalDialogs : SimpleModalBorder {
    private val type: Type
    private var titleComponent: Component? = null

    constructor(
        type: Type,
        message: String?,
        title: String?,
        optionType: Option?,
        callback: ModalCallback?
    ) : this(type, createMessage(type, message), title, optionType, callback)

    constructor(
        type: Type,
        message: String?,
        title: String?,
        optionType: Array<Option?>?,
        callback: ModalCallback?
    ) : this(type, createMessage(type, message), title, optionType, callback)

    constructor(type: Type, message: String?, title: String?, optionType: Int, callback: ModalCallback?) : this(
        type,
        createMessage(type, message),
        title,
        optionType,
        callback
    )

    constructor(
        type: Type,
        messageComponent: Component?,
        title: String?,
        optionType: Option?,
        callback: ModalCallback?
    ) : super(messageComponent, title, arrayOf<Option?>(optionType), callback) {
        this.type = type
    }

    constructor(
        type: Type,
        messageComponent: Component?,
        title: String?,
        optionType: Array<Option?>?,
        callback: ModalCallback?
    ) : super(messageComponent, title, optionType, callback) {
        this.type = type
    }

    constructor(
        type: Type,
        messageComponent: Component?,
        title: String?,
        optionType: Int,
        callback: ModalCallback?
    ) : super(messageComponent, title, optionType, callback) {
        this.type = type
    }

    constructor(
        type: Type,
        message: String?,
        titleComponent: Component?,
        optionType: Int,
        callback: ModalCallback?
    ) : this(type, createMessage(type, message), titleComponent, optionType, callback)

    constructor(
        type: Type,
        messageComponent: Component?,
        titleComponent: Component?,
        optionType: Int,
        callback: ModalCallback?
    ) : super(messageComponent, null, optionType, callback) {
        this.titleComponent = titleComponent
        this.type = type
    }

    override fun createTitleComponent(title: String?): JComponent? {
        if (titleComponent != null && titleComponent is JComponent) {
            return titleComponent as JComponent
        }
        if (type == Type.DEFAULT) {
            return super.createTitleComponent(title)
        }
        val icon = createIcon(type)
        val label = super.createTitleComponent(title) as JLabel
        label.setIconTextGap(10)
        label.setIcon(icon)
        return label
    }

    override fun createOptionButton(optionsType: Array<Option?>?): JComponent {
        val panel = super.createOptionButton(optionsType) as JPanel
        // modify layout option
        if (panel.layout is MigLayout) {
//            layout.setColumnConstraints("[]12[]")
        }

        // revers order
        val components = panel.components
        panel.removeAll()
        for (i in components.indices.reversed()) {
            panel.add(components[i])
        }
        return panel
    }

    override fun createButtonOption(option: Option?): JButton {
        val button = super.createButtonOption(option)
        val colors = getColorKey(type)
        if (button.isDefaultButton()) {
            button.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:999;" + "margin:3,33,3,33;" + "borderWidth:0;" + "focusWidth:0;" + "innerFocusWidth:0;" + "default.borderWidth:0;" + "[light]background:" + colors[0] + ";" + "[dark]background:" + colors[1] + ";"
            )
        } else {
            button.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:999;" + "margin:3,33,3,33;" + "borderWidth:1;" + "focusWidth:0;" + "innerFocusWidth:1;" + "background:null;" + "[light]borderColor:" + colors[0] + ";" + "[dark]borderColor:" + colors[1] + ";" + "[light]focusedBorderColor:" + colors[0] + ";" + "[dark]focusedBorderColor:" + colors[1] + ";" + "[light]focusColor:" + colors[0] + ";" + "[dark]focusColor:" + colors[1] + ";" + "[light]hoverBorderColor:" + colors[0] + ";" + "[dark]hoverBorderColor:" + colors[1] + ";" + "[light]foreground:" + colors[0] + ";" + "[dark]foreground:" + colors[1] + ";"
            )
        }
        return button
    }

    protected fun createIcon(type: Type): Icon {
        val data: ThemesData = Toast.getThemesData().get(asToastType(type))!!
        val icon = FlatSVGIcon(data.getIcon(), 0.45f)
        val colorFilter = ColorFilter()
        colorFilter.add(Color.decode("#969696"), Color.decode(data.getColors()[0]), Color.decode(data.getColors()[1]))
        icon.setColorFilter(colorFilter)
        return icon
    }

    protected fun getColorKey(type: Type): Array<String?> {
        if (type == Type.DEFAULT) {
            // use accent color as default type
            return arrayOf<String>("\$Component.accentColor", "\$Component.accentColor") as Array<String?>
        }
        val data: ThemesData = Toast.getThemesData().get(asToastType(type))!!
        return data.getColors()
    }

    private fun asToastType(type: Type): Toast.Type {
        return when (type) {
            Type.DEFAULT -> Toast.Type.DEFAULT
            Type.SUCCESS -> Toast.Type.SUCCESS
            Type.INFO -> Toast.Type.INFO
            Type.WARNING -> Toast.Type.WARNING
            else -> Toast.Type.ERROR
        }
    }

    enum class Type {
        DEFAULT, SUCCESS, INFO, WARNING, ERROR
    }

    companion object {
        private fun createMessage(type: Type?, message: String?): Component {
            val text = JTextArea(message)
            text.setWrapStyleWord(true)
            text.setEditable(false)
            text.setCaret(object : DefaultCaret() {
                override fun paint(g: Graphics?) {
                }
            })
            val gap = if (type == Type.DEFAULT) "30" else "62"
            text.putClientProperty(
                FlatClientProperties.STYLE,
                "border:0," + gap + ",10,30;" + "[light]foreground:lighten(\$Label.foreground,20%);" + "[dark]foreground:darken(\$Label.foreground,20%);"
            )
            return text
        }
    }
}
