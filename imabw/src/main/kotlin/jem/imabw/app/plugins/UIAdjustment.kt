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

package jem.imabw.app.plugins

import jclp.util.Reflections
import mala.core.Plugin
import mala.ixin.IxIn
import java.util.*

class UIAdjustment : Plugin {
    override val meta = mapOf("name" to "UIAdjustment", "version" to "1.0")

    override fun init() {
        val theme = IxIn.getThemePath(IxIn.swingTheme)
        if ("jtattoo" in theme) {
            initJTattoo(theme)
        }
    }

    private fun initJTattoo(theme: String) {
        val prop = Properties()
        prop["logoString"] = "PW Imabw"
        prop["textAntiAliasing"] = "on"
        prop["centerWindowTitle"] = "off"
        Reflections.Invocation.builder()
                .target(Class.forName(theme))
                .name("setCurrentTheme")
                .types(arrayOf(Properties::class.java))
                .arguments(arrayOf(prop))
                .build()
                .invoke()
    }
}
