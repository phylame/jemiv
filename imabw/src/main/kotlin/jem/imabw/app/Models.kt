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

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import jem.Book
import jem.epm.EpmManager
import jem.epm.util.ParserParam
import jem.imabw.app.ui.Dashboard
import mala.core.App
import mala.core.App.tr
import mala.ixin.Command
import mala.ixin.CommandHandler

object Workbench : CommandHandler {
    val epmManager by lazy { EpmManager() }

    val activeWork: Work? = null

    fun start() {
        val param = ParserParam.builder()
                .input("E:/tmp/1.pmab")
                .build()
        Observable.create<Book> {
            it.onNext(epmManager.readBook(param))
            it.onComplete()
        }.observeOn(Schedulers.io())
                .observeOn(SwingScheduler)
                .doOnSubscribe {
                    Dashboard.isWaiting = true
                }
                .subscribe {
                    Dashboard.isWaiting = false
                    println(it)
                }
    }

    fun ensureSaved(title: String): Boolean {
        return true
    }

    @Command
    fun exit() {
        if (ensureSaved(tr("d.exit.title"))) {
            App.exit()
        }
    }

    override fun performed(command: String): Boolean {
        when (command) {
            else -> return false
        }
        return true
    }
}

class Work {
    val isModified get() = false

    fun dispose() {}
}
