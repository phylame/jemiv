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
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

fun <T> postEvent(action: () -> T, success: (T) -> Unit) {
    Observable.create<T> {
        it.onNext(action())
        it.onComplete()
    }.subscribeOn(Schedulers.computation())
            .observeOn(SwingScheduler)
            .subscribe {
                success(it)
            }
}

object SwingScheduler : Scheduler() {
    override fun createWorker(): Worker = SwingWorker

    private object SwingWorker : Worker() {
        override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
            SwingUtilities.invokeLater(run)
            return DummyDisposable
        }

        override fun dispose() {
        }

        override fun isDisposed() = true
    }

    private object DummyDisposable : Disposable {
        override fun isDisposed() = true

        override fun dispose() {}
    }
}
