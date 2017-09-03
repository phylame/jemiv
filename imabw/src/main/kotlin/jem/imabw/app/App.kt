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

package jem.imabw.app

import jem.imabw.app.contents.ContentsPane
import jem.imabw.app.editor.EditorPane
import jem.imabw.app.ui.Dashboard
import jem.util.Build
import mala.core.App
import mala.core.App.resourceManager
import mala.ixin.IDelegate
import mala.ixin.IxIn
import java.util.*

fun main(args: Array<String>) {
    App.run(Imabw, args)
}

object Imabw : IDelegate() {
    override val name = "imabw"

    override val version = Build.VERSION

    override fun onStart() {
        Locale.setDefault(Locale.ENGLISH)
        App.translator = resourceManager.linguistFor("i18n/dev/app")
    }

    override fun onInit() {
        IxIn.initSwing()
        Dashboard.isVisible = true
    }

    override fun onReady() {
        postEvent({
            proxy.register(Workbench, ContentsPane, EditorPane)
        }, {
            Workbench.start()
        })
    }

    override fun performed(command: String): Boolean {
        when (command) {
            "editSettings" -> {
            }
            "findAction" -> {
            }
            "help" -> {
            }
            "about" -> {
            }
            "garbageCollect" -> System.gc()
            else -> return super.performed(command)
        }
        return true
    }
}
