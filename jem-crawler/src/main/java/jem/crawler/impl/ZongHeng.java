package jem.crawler.impl;

import static jclp.util.StringUtils.firstPartOf;
import static jclp.util.StringUtils.secondPartOf;
import static jclp.util.StringUtils.valueOfName;
import static jem.Attributes.setAuthor;
import static jem.Attributes.setCover;
import static jem.Attributes.setDate;
import static jem.Attributes.setGenre;
import static jem.Attributes.setIntro;
import static jem.Attributes.setState;
import static jem.Attributes.setTitle;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import jclp.util.CollectionUtils;
import jclp.util.DateUtils;
import jem.Book;
import jem.crawler.AbstractCrawler;
import jem.crawler.Crawler;
import jem.crawler.CrawlerBook;
import jem.crawler.CrawlerFactory;
import jem.crawler.M;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.flob.Flobs;
import lombok.val;

public class ZongHeng extends AbstractCrawler implements CrawlerFactory {
    @Override
    public Book getBook(String url, TypedConfig config) throws JemException, IOException {
        if (url.contains("m.zongheng.com")) {
            val bookId = valueOfName(secondPartOf(url, "?"), "bookid", "&");
            url = firstPartOf(url, "//") + "//book.zongheng.com/book/" + bookId + ".html";
        } else {
            url = url.replace("showchapter", "book");
        }
        Document doc;
        try {
            doc = getSoup(url, config);
        } catch (InterruptedException e) {
            throw new JemException("interrupted", e);
        }
        val book = new CrawlerBook();
        val head = doc.select("head");
        setTitle(book, getMeta(head, "title"));
        setIntro(book, getMeta(head, "description"));
        setCover(book, Flobs.forURL(new URL(getMeta(head, "image"))));
        setGenre(book, getMeta(head, "novel:category"));
        setAuthor(book, getMeta(head, "novel:autho"));
        setState(book, getMeta(head, "novel:status"));
        setDate(book, DateUtils.parse(getMeta(head, "novel:update_time"), new Date()));
        return book;
    }

    private String getMeta(Elements base, String name) {
        val key = name.startsWith("novel") ? "name=og:" : "property=og:";
        val meta = base.select("meta[" + key + name + "]").first();
        return meta != null ? meta.attr("content") : "";
    }

    @Override
    public String getText(String url, TypedConfig config) throws JemException, IOException {
        return null;
    }

    @Override
    public Crawler getCrawler() {
        return this;
    }

    @Override
    public String getName() {
        return M.translator().tr("zongheng.com");
    }

    @Override
    public Set<String> getNames() {
        return CollectionUtils.setOf("book.zongheng.com", "m.zongheng.com");
    }
}
