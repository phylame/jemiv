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

import jclp.io.IOUtils;
import jem.Book;
import jem.Chapter;
import jem.crawler.*;
import jem.util.JemException;
import jem.util.TypedConfig;
import lombok.val;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Set;

import static jclp.util.CollectionUtils.setOf;
import static jclp.util.StringUtils.trimmed;
import static jem.Attributes.*;
import static jem.crawler.SoupUtils.firstText;
import static jem.crawler.SoupUtils.queryLink;
import static jem.crawler.SoupUtils.queryText;

public class Qidian extends ReusedCrawler {
    @Override
    public Book getBook(String url, TypedConfig config) throws JemException, IOException {
        if (url.contains("m.qidian.com")) {
            url = url.replace("m.qidian.com/book", "book.qidian.com/info");
        } else {
            url = url.replace("#Catalog", "");
        }
        val doc = getSoup(url, config);
        val book = new CrawlerBook();
        val stub = doc.select("div.book-information div.book-info");
        setTitle(book, queryText(stub, "h1 em"));
        val src = queryLink(doc, "a#bookImg img");
        if (src != null) {
            setCover(book, getFlob(src.replaceFirst("[\\d]+$", "600") + ".jpg", config));
        }
        setAuthor(book, queryText(stub, "h1 a"));
        setState(book, queryText(stub, "p.tag span:eq(0)"));
        setGenre(book, queryText(stub, "p.tag a", "/"));
        setValue(book, "brief", queryText(doc, "p.intro"));
        setWords(book, queryText(stub, "p em:eq(0)") + queryText(stub, "p cite:eq(1)"));
        setIntro(book, queryText(doc, "div.book-intro p", System.lineSeparator()));
        getContents(book, doc, config);
        return book;
    }

    private void getContents(Book book, Document doc, TypedConfig config) throws IOException, JemException {
        val toc = doc.select("div.volume-wrap div.volume");
        if (toc.isEmpty()) {
            getMobileContents(book, doc.baseUri().substring(doc.baseUri().lastIndexOf("/")), config);
            return;
        }
        for (val div : toc) {
            val section = book.newChapter(firstText(div.select("h3").first()));
            setValue(section, WORDS, queryText(div, "h3 cite"));
            for (val a : div.select("li a")) {
                val url = a.absUrl("href");
                val chapter = section.newChapter(trimmed(a.text()));
                chapter.setText(new CrawlerText(url, this, config, chapter));
                setValue(chapter, "source", url);
            }
        }
    }

    private void getMobileContents(Book book, String bookId, TypedConfig config) throws JemException, IOException {
        val baseURL = "http://m.qidian.com/book/" + bookId;
        JSONArray parts;
        try (val in = fetchStream(baseURL + "/catalog", "get", config)) {
            val data = IOUtils.toString(in, "utf-8");
            val start = data.indexOf("[{", data.indexOf("g_data.volumes"));
            parts = new JSONArray(data.substring(start, data.lastIndexOf("}];") + 2));
        }
        Chapter section;
        for (val se : parts) {
            JSONObject json = (JSONObject) se;
            section = book.newChapter(trimmed(json.getString("vN")));
            for (val ch : json.getJSONArray("cs")) {
                json = (JSONObject) ch;
                val chapter = section.newChapter(trimmed(json.getString("cN")));
                setWords(chapter, json.getInt("cnt"));
                val url = baseURL + '/' + json.getInt("id");
                val text = new CrawlerText(url, this, config, chapter);
                setValue(chapter, "source", url);
                chapter.setText(text);
            }
        }
    }

    @Override
    public String getText(String url, TypedConfig config) throws JemException, IOException {
        val doc = getSoup(url, config);
        return url.contains("m.qidian.com")
                ? queryText(doc, "article#chapterContent p", System.lineSeparator())
                : queryText(doc, "div.read-content p", System.lineSeparator());
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
