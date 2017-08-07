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

package jem.crawler.impl;

import jem.Book;
import jem.Chapter;
import jem.crawler.*;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.flob.Flobs;
import lombok.val;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static jclp.util.CollectionUtils.setOf;
import static jclp.util.StringUtils.trimmed;
import static jem.Attributes.*;
import static jem.crawler.SoupUtils.*;

public class QidianCom extends AbstractCrawler implements CrawlerFactory {
    @Override
    public Book getBook(String url, TypedConfig config) throws JemException, IOException {
        if (url.contains("m.qidian.com")) {
            url = url.replace("m.qidian.com/book", "book.qidian.com/info");
        } else {
            url = url.replace("#Catalog", "");
        }
        Document doc;
        try {
            doc = getSoup(url, config);
        } catch (InterruptedException e) {
            throw new JemException("interrupted", e);
        }
        val book = new CrawlerBook();
        val info = doc.select("div.book-information div.book-info");
        setTitle(book, queryText(info, "h1 em"));
        val src = queryLink(doc, "a#bookImg img");
        if (src != null) {
            setCover(book, Flobs.forURL(new URL(src.replaceFirst("[\\d]+$", "600") + ".jpg"), "image/jpg"));
        }
        setAuthor(book, queryText(info, "h1 a"));
        setValue(book, KEYWORDS, queryText(info, "p.tag a", VALUES_SEPARATOR));
        setValue(book, "brief", queryText(doc, "p.intro"));
        setWords(book, queryText(info, "p em:eq(0)") + queryText(info, "p cite:eq(1)"));
        setIntro(book, queryText(doc, "div.book-intro p", "\n"));
        getContents(book, doc, config);
        return book;
    }

    private void getContents(Book book, Document doc, TypedConfig config) throws IOException, JemException {
        val toc = doc.select("div.volume-wrap div.volume");
        if (toc.isEmpty()) {
//            fetchContents(book, doc.baseUri() + "#Catalog", config);
            return;
        }
        for (val div : toc) {
            val section = new Chapter(firstText(div.select("h3").first()));
            setValue(section, WORDS, queryText(div, "h3 cite"));
            book.append(section);
            for (val a : div.select("li a")) {
                val chapter = new Chapter(trimmed(a.text()));
                chapter.setText(new CrawlerText(a.absUrl("href"), this, config, chapter));
                section.append(chapter);
            }
        }
    }

    private void fetchContents(Book book, String url, TypedConfig config) throws JemException, IOException {
        Document doc;
        try {
            doc = getSoup(url, config);
        } catch (InterruptedException e) {
            throw new JemException("interrupted", e);
        }
//        System.out.println(doc.select("div.volume-wrap"));
//        getContents(book, doc, config);
    }

    @Override
    public String fetchText(String url, TypedConfig config) throws JemException, IOException {
        Document doc;
        try {
            doc = getSoup(url, config);
        } catch (InterruptedException e) {
            throw new JemException("interrupted", e);
        }
        return queryText(doc, "div.read-content p", System.lineSeparator());
    }

    @Override
    public Crawler getCrawler() {
        return this;
    }

    @Override
    public String getName() {
        return M.translator().tr("qidian.com");
    }

    @Override
    public Set<String> getNames() {
        return setOf("book.qidian.com", "m.qidian.com");
    }
}
