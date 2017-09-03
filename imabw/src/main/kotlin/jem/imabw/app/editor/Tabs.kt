/*
 * Copyright 2017 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jem.imabw.app.editor

import jem.Chapter
import jem.imabw.app.ui.Dashboard
import jem.imabw.app.ui.Indicator
import jem.imabw.app.ui.registerFormAction
import jem.kotlin.title
import mala.core.App.resourceManager
import mala.core.App.tr
import mala.core.toRoot
import mala.ixin.*
import org.jdesktop.swingx.JXTextArea
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class Tab(val chapter: Chapter) : JScrollPane(), ITab {
    override val titleBar = TabHeader()

    override val titleTip get() = chapter.toRoot().joinToString(" > ")

    private val textArea = JXTextArea(tr("form.editor.prompt"))

    private val undoHelper = object : UndoHelper() {
        override fun stateChanged() {
            titleBar.setTitle(chapter.title + if (isModified) "*" else "")
        }
    }

    init {
        registerTextActions()
        textArea.document.addUndoableEditListener(undoHelper)
        textArea.addCaretListener {
            updateIndicator()
            updateEditActions()
        }
        textArea.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                updateIndicator()
                updateEditActions()
            }
        })
        textArea.toolTipText = null
        titleBar.setTitle(chapter.title)
        setViewportView(textArea)
    }

    private fun registerTextActions() {
        textArea.registerCommonActions()
        textArea.registerFormAction("undo") { undoHelper.undoIfNeed() }
        textArea.registerFormAction("redo") { undoHelper.redoIfNeed() }
        textArea.registerFormAction("joinLine") {

        }
        textArea.registerFormAction("duplicateText") {

        }
    }

    private fun updateIndicator() {
        Indicator.updateWords(textArea.document.length)
        Indicator.updateCaret(textArea.row + 1, textArea.column + 1, textArea.selectionLength)
    }

    private fun updateEditActions() {
        val editable = textArea.isEditable
        val hasSelection = textArea.selectionLength > 0
        var action = Dashboard["undoHelper"]
        if (action != null) {
            action.isEnabled = editable && undoHelper.canUndo()
            action[Action.NAME] = undoHelper.undoPresentationName
            action[Action.SHORT_DESCRIPTION] = undoHelper.undoPresentationName
        }
        action = Dashboard["redo"]
        if (action != null) {
            action.isEnabled = editable && undoHelper.canRedo()
            action[Action.NAME] = undoHelper.redoPresentationName
            action[Action.SHORT_DESCRIPTION] = undoHelper.redoPresentationName
        }
        Dashboard["cut"] = editable && hasSelection
        Dashboard["copy"] = editable && hasSelection
        val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
        Dashboard["paste"] = editable && contents?.isDataFlavorSupported(DataFlavor.stringFlavor) == true
        Dashboard["delete"] = editable
    }
}

class TabHeader : JPanel() {
    private val label = JLabel()

    init {
        isOpaque = false
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        label.iconTextGap = 0
        this += label

        val button = Dashboard.getOrPut("closeActiveTab").toImageButton()
        button.rolloverIcon = resourceManager.iconFor("tab/close-rollover")
        button.icon = resourceManager.iconFor("tab/close")
        button.isFocusable = false
        button.border = null
        this += button
    }

    fun setIcon(icon: Icon) {
        label.icon = icon
    }

    fun setTitle(title: String) {
        label.text = title
    }
}
