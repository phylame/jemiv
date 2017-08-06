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

import jclp.setting.PropertiesSettings;
import jclp.util.CollectionUtils;
import jclp.util.StringUtils;
import jem.Chapter;
import jem.crawler.CrawlerBook;
import jem.crawler.CrawlerListener;
import jem.crawler.CrawlerManager;
import jem.epm.EpmManager;
import jem.epm.util.MakerParam;
import lombok.val;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        crawlerTest();
    }

    private static void crawlerTest() throws Exception {
        val settings = new PropertiesSettings();
        settings.set("maker.vdm.type", "zip");
//        settings.set("maker.xml.encoding", "gbk");
        settings.set("maker.xml.indent", "\t");
        settings.set("maker.pmab.meta", CollectionUtils.mapOf("version", "3.0", "generator", "jem v4.0"));
        long begin = System.currentTimeMillis();
        CrawlerManager cm = new CrawlerManager();
        System.out.println(cm.getService("book.qidian.com").getName());
        val executor = Executors.newFixedThreadPool(80);
        val book = cm.fetchBook("http://m.qidian.com/book/1005401934", settings);
        int totals = 0;
        if (book instanceof CrawlerBook) {
            val crawlerBook = (CrawlerBook) book;
            crawlerBook.schedule(executor);
            totals = crawlerBook.getTotals();
        }
        val n = totals;
        settings.set("crawler.listener", new CrawlerListener() {
            private AtomicInteger i = new AtomicInteger();

            @Override
            public void textFetched(Chapter chapter, String url) {
                String str = StringUtils.duplicated("#", 100);
                int i = (int) (str.length() * (this.i.addAndGet(1) / (double) n));
                String s = StringUtils.duplicated("#", i) + StringUtils.duplicated(" ", str.length() - i);
//                System.out.printf("\r%d/%d %s: %s", i.addAndGet(1), n, getString(chapter, TITLE), url);
                System.out.print("\r[" + s + "]");
                System.out.flush();
            }
        });
        book.getExtensions().set("date", new Date());
        System.out.println();
        try {
            new EpmManager().writeBook(MakerParam.builder()
                    .book(book)
                    .format("pmab")
                    .file(new File("E:/tmp/3"))
                    .arguments(settings)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
        System.out.println(System.currentTimeMillis() - begin);
        book.cleanup();
    }
}
