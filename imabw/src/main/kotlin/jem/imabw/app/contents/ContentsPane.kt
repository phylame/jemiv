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

package jem.imabw.app.contents

import jem.imabw.app.Imabw
import jem.imabw.app.editor.EditorPane
import jem.imabw.app.ui.Dashboard
import mala.core.App
import mala.core.App.resourceManager
import mala.core.App.tr
import mala.ixin.*
import org.jdesktop.swingx.JXTree
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

object ContentsPane : JPanel(BorderLayout()) {
    init {
        add(NavigationHeader, BorderLayout.PAGE_START)
        initTree()
    }

    private fun initTree() {
        val root = DefaultMutableTreeNode("Root")
        for (i in 1..16) {
            val node = DefaultMutableTreeNode()
            if (i % 3 == 0) {
                node.userObject = "$i. Section"
                for (j in 1..5) {
                    node.add(DefaultMutableTreeNode("$i.$j - Chapter"))
                }
            } else {
                node.userObject = "$i. Chapter"
            }
            root.add(node)
        }

        val tree = JXTree(DefaultTreeModel(root))
        tree.isRootVisible = false
        val pane = JScrollPane(tree)
        pane.border = BorderFactory.createEmptyBorder()
        add(pane, BorderLayout.CENTER)
    }

    @Command
    fun newChapter() {
        EditorPane.newTab()
    }
}

object NavigationHeader : JPanel(BorderLayout()) {
    val toolbar: JToolBar

    private val actions = arrayOf("newChapter", "renameChapter", "viewAttributes")

    init {
        val label = JLabel(tr("form.contents.title"), resourceManager.iconFor("tree/contents"), JLabel.LEADING)
        add(label, BorderLayout.LINE_START)
        label.iconTextGap = 2

        toolbar = JToolBar()
        toolbar.isRollover = true
        toolbar.isFloatable = false
        toolbar.isBorderPainted = false
        toolbar.add(Box.createRigidArea(8 x 0))
        toolbar.addSeparator()
        for (id in actions) {
            toolbar.attach(Dashboard.actions.get(id, Imabw, App, resourceManager).toButton(Style.PLAIN, Dashboard))
        }
        add(toolbar, BorderLayout.LINE_END)
    }
}
