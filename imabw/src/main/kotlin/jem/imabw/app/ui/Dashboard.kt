package jem.imabw.app.ui

import jem.imabw.app.Imabw
import jem.imabw.app.contents.ContentsPane
import jem.imabw.app.editor.EditorPane
import mala.core.App
import mala.core.App.resourceManager
import mala.ixin.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

object Dashboard : IForm() {
    init {
        init(resourceManager.designerFor("ui/actions")!!)
        actions.updateKeys(resourceManager.keymapFor("ui/keys"))
        title = "Untitled - PW Imabw ${Imabw.version}"
        initComponents()
        size = 1067 x 600
    }

    override fun createStatusBar() {
        super.createStatusBar()
        statusBar?.add(Indicator, BorderLayout.LINE_END)
        statusBar?.border = BorderFactory.createEmptyBorder()
        statusBar?.label?.border = BorderFactory.createEmptyBorder(0, 2, 0, 0)
    }

    private fun initComponents() {
        val pane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, ContentsPane, EditorPane)
        contentPane.add(pane, BorderLayout.CENTER)
    }

    @Command
    fun exit() {
        App.exit()
    }
}

private class ActionAdapter(val action: (ActionEvent) -> Unit) : ActionListener {
    override fun actionPerformed(e: ActionEvent) {
        action(e)
    }
}

fun JComponent.registerFormAction(id: String, scope: Int = JComponent.WHEN_FOCUSED, action: (ActionEvent) -> Unit) {
    val value = Dashboard[id]?.getValue(Action.ACCELERATOR_KEY)
    if (value is KeyStroke) {
        registerKeyboardAction(ActionAdapter(action), value, scope)
    }
}

object Indicator : JPanel() {
    private val padding by lazy { BorderFactory.createEmptyBorder(0, 4, 0, 4) }

    private val caret = JLabel()

    private val words = JLabel()

    private val mimeType = JLabel()

    private val encoding = JLabel()

    init {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)

        caret.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 1 && e.isLeft) {
                    Imabw.performed("goto")
                }
            }
        })

        this += caret
        this += words
        this += mimeType
        this += encoding

        var button = Dashboard.getOrPut("lockFile").toImageButton(Style.TOGGLE)
        button.selectedIcon = resourceManager.iconFor("status/locked")
        button.icon = resourceManager.iconFor("status/unlocked")
        this += button

        button = Dashboard.getOrPut("viewMessage").toImageButton()
        button.icon = resourceManager.iconFor("status/message")
        this += button

        button = Dashboard.getOrPut("garbageCollect").toImageButton()
        button.icon = resourceManager.iconFor("status/gc")
        this += button

        updateCaret(-1, -1, 0)
        updateWords(-1)
        updateMimeType("")
        updateEncoding("")
    }

    fun updateCaret(row: Int, column: Int, selection: Int) {
        caret.text = when {
            row < 0 -> "n/a"
            selection > 0 -> "$row:$column/$selection"
            else -> "$row:$column"
        }
    }

    fun updateWords(count: Int) {
        words.text = if (count < 0) "n/a" else count.toString()
    }

    fun updateMimeType(type: String) {
        mimeType.text = if (type.isEmpty()) "n/a" else type
    }

    fun updateEncoding(name: String) {
        encoding.text = if (name.isEmpty()) "n/a" else name
    }

    override fun addImpl(comp: Component, constraints: Any?, index: Int) {
        super.addImpl(JSeparator(JSeparator.VERTICAL), constraints, index)
        comp.isFocusable = false
        if (comp is JComponent) {
            comp.border = padding
        }
        super.addImpl(comp, constraints, index)
    }
}
