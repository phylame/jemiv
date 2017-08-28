package jem.crawler.impl;

import jclp.util.DateUtils;
import jem.Book;
import jem.crawler.*;
import jem.util.JemException;
import jem.util.TypedConfig;
import lombok.val;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static jclp.util.CollectionUtils.setOf;
import static jem.Attributes.*;

public class Duxia extends ReusedCrawler {

    @Override
    public Book getBook(String url, TypedConfig config) throws JemException, IOException {
        if (url.contains("wap.duxia.org")) {
            url = url.replace("wap", "www");
        } else {
            url = url.replaceFirst("/du/[\\d]+/([\\d]+)/", "/book/$1.html");
        }
        val book = new CrawlerBook();
        Document doc = getSoup(url, config);

        Elements stub = doc.select("head");
        setTitle(book, getMeta(stub, "title"));
        setCover(book, getFlob(getMeta(stub, "image"), config));
        setGenre(book, getMeta(stub, "novel:category"));
        setAuthor(book, getMeta(stub, "novel:author"));
        setState(book, getMeta(stub, "novel:status"));
        setDate(book, DateUtils.parse(getMeta(stub, "novel:update_time"), "yyyy-MM-dd hh:mm:ss", new Date()));

        stub = doc.select("div.articleInfoRight");
        setWords(book, stub.select("ol p:eq(0) strong:eq(6)").text());
        setIntro(book, SoupUtils.queryText(stub, "#wrap", "\n"));

        doc = getSoup(stub.select("a.reader").first().absUrl("href"), config);
        for (val a : doc.select("table.acss a")) {
            url = a.absUrl("href");
            val chapter = book.newChapter(a.text().trim());
            chapter.setText(new CrawlerText(url, this, config, chapter));
            setValue(chapter, "source", url);
        }

        return book;
    }

    private String getMeta(Elements soup, String name) {
        return soup.select("meta[property=og:" + name + "]").first().attr("content");
    }

    @Override
    public String getText(String url, TypedConfig config) throws JemException, IOException {
        return "";
    }

    @Override
    public String getName() {
        return M.translator().tr("duxia.org");
    }

    @Override
    public Set<String> getNames() {
        return setOf("www.duxia.org", "wap.duxia.org");
    }
}
