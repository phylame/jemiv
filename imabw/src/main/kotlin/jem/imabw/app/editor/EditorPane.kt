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

import jem.Book
import jem.imabw.app.ui.Dashboard
import mala.ixin.*

object EditorPane : ITabbedPane(), TabListener, CommandHandler {
    init {
        isFocusable = false
        addTabListener(this)
        tabLayoutPolicy = SCROLL_TAB_LAYOUT
        newTab()
    }

    fun newTab() {
        val book = Book("Example")
        val section = book.newChapter("Section 1")
        val chapter = section.newChapter("Chapter 1")
        addTab(null, Tab(chapter))
        selectedIndex = tabCount - 1
    }

    override fun tabCreated(e: TabEvent) {
        updateTabActions()
    }

    override fun tabClosed(e: TabEvent) {
        updateTabActions()
    }

    override fun performed(command: String): Boolean {
        when (command) {
            else -> return (selectedComponent as? CommandHandler)?.performed(command) == true
        }
        return true
    }

    private fun updateTabActions() {
        val count = tabCount
        Dashboard["nextTab"] = count > 1
        Dashboard["previousTab"] = count > 1
        Dashboard["closeActiveTab"] = count != 0
        Dashboard["closeOtherTabs"] = count > 1
        Dashboard["closeAllTabs"] = count != 0
        Dashboard["closeUnmodifiedTabs"] = count != 0
    }
}
