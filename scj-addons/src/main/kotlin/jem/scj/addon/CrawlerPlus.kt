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

package jem.scj.addon

import jclp.value.Values
import jem.Chapter
import jem.crawler.CrawlerBook
import jem.crawler.CrawlerListener
import jem.epm.util.MakerParam
import jem.epm.util.ParserParam
import jem.kotlin.title
import jem.scj.app.SCI
import jem.scj.app.SCIPlugin
import jem.scj.app.SCISettings
import mala.cli.action
import mala.core.App
import org.apache.commons.cli.Option
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CrawlerPlus : SCIPlugin, CrawlerListener {
    private val threadPool = Values.lazy {
        val threads = SCISettings.get("crawler.maxThread", Int::class.java) as?Int
        Executors.newFixedThreadPool(threads ?: Runtime.getRuntime().availableProcessors() * 8)
    }

    private var totals: AtomicInteger = AtomicInteger()

    private var current: AtomicInteger = AtomicInteger()

    override val meta = mapOf("name" to "Crawler Plus")

    init {
        Supports.attachTranslator()
        Option.builder()
                .longOpt("list-crawlers")
                .desc(App.tr("opt.listCrawlers.desc"))
                .action { _ ->
                    val plugins = SCI.crawlerManager.services.map {
                        it.name + "\n" + it.names.joinToString("\n") { "  " + it }
                    }
                    println(App.tr("listCrawlers.legend", plugins.size))
                    println(plugins.joinToString("\n"))
                    0
                }
    }

    override fun onOpenBook(param: ParserParam) {
        if (param.format == "crawler") {
            param.arguments?.set("crawler.listener", this)
            param.arguments?.set("crawler.crawlerManager", SCI.crawlerManager)
        }
    }

    override fun onSaveBook(param: MakerParam) {
        val book = param.book
        if (book is CrawlerBook) {
            current.set(1)
            book.schedule(threadPool.get())
            totals.set(book.totals)
        }
    }

    override fun onBookSaved(param: MakerParam) {
        print("\r")
        System.out.flush()
    }

    override fun destroy() {
        if (threadPool.isInitialized) {
            val pool = threadPool.get()
            pool.shutdown()
        }
    }

    override fun textFetched(chapter: Chapter, url: String) {
        println("${current.getAndIncrement()}/${totals.get()}: ${chapter.title}")
    }
}
