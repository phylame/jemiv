package jem.crawler.impl;

import jclp.util.CollectionUtils;
import jclp.util.DateUtils;
import jem.Book;
import jem.crawler.*;
import jem.util.JemException;
import jem.util.TypedConfig;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

import static jclp.util.StringUtils.*;
import static jem.Attributes.*;
import static jem.crawler.SoupUtils.queryLink;
import static jem.crawler.SoupUtils.queryText;

public class ZongHeng extends ReusedCrawler {
    @Override
    public Book getBook(String url, TypedConfig config) throws JemException, IOException {
        val book = new CrawlerBook();
        if (url.contains("m.zongheng.com")) {
            fetchBookMobile(book, url, config);
        } else {
            url = url.replace("showchapter", "book");
            fetchBookPC(book, url, config);
        }
        return book;
    }

    private void fetchBookPC(Book book, String url, TypedConfig config) throws IOException, JemException {
        val doc = getSoup(url, config);
        Elements stub = doc.select("head");
        setTitle(book, getMeta(stub, "title"));
        setIntro(book, getMeta(stub, "description"));
        setCover(book, getFlob(getMeta(stub, "image")));
        setGenre(book, getMeta(stub, "novel:category"));
        setAuthor(book, getMeta(stub, "novel:author"));
        setState(book, getMeta(stub, "novel:status"));
        setDate(book, DateUtils.parse(getMeta(stub, "novel:update_time"), "yyyy-MM-dd", new Date()));
        stub = doc.select("div.status");
        setWords(book, stub.select("span[title]").text());
        setValue(book, KEYWORDS, queryText(stub, "div.keyword a", VALUES_SEPARATOR));
        fetchContentsPC(book, url, config);
    }

    private String getMeta(Elements base, String name) {
        val key = name.startsWith("novel") ? "name=og:" : "property=og:";
        val meta = base.select("meta[" + key + name + "]").first();
        return meta != null ? meta.attr("content") : "";
    }

    private void fetchContentsPC(Book book, String url, TypedConfig config) throws JemException, IOException {
        url = url.replace("/book/", "/showchapter/");
        val doc = getSoup(url, config);
        val stub = doc.select("div#chapterListPanel");
        val titles = stub.select("h5").iterator();
        val sections = stub.select("div.booklist").iterator();
        while (titles.hasNext()) {
            val section = book.newChapter(trimmed(titles.next().text()));
            for (val a : sections.next().select("a")) {
                val chapter = section.newChapter(trimmed(a.text()));
                chapter.setText(new CrawlerText(a.absUrl("href"), this, config, chapter));
            }
        }
    }

    private void fetchBookMobile(Book book, String url, TypedConfig config) throws IOException {
        val doc = getSoup(url, config);
        Elements stub = doc.select("div.booksite");
        setCover(book, getFlob(queryLink(stub, "img")));
        stub = stub.select("div.bookinfo");
        setTitle(book, queryText(stub, "h1"));
        stub = stub.select("div.info");
        setAuthor(book, queryText(stub, "div div:eq(0) span"));
        setGenre(book, queryText(stub, "div div:eq(1) span"));
        setWords(book, queryText(stub, "div div:eq(2) span"));
        setIntro(book, queryText(doc, "div.book_intro", System.lineSeparator()));
        setValue(book, KEYWORDS, queryText(doc, "div.tags_wap", VALUES_SEPARATOR));
        val bookId = valueOfName(secondPartOf(url, "?"), "bookid", "&");
        config.set("bookId", bookId);
        fetchToc(new Local(book, StringUtil.resolve(doc.baseUri(), "/h5/ajax/chapter"), bookId, config));
    }

    private static final String TEXT_PATTERN = "http://m.zongheng.com/h5/ajax/chapter?bookId=%s&chapterId=%s";
    private static final String PARAGRAPH_END = "。？”！…※）》】";

    @Override
    protected int fetchPage(int page, Object arg) throws IOException {
        val data = (Local) arg;
        val pageSize = data.config.getInt("crawler.zongheng.pageSize", 180);
        String url = data.url + String.format("/list?h5=1&bookId=%s&pageNum=%s&pageSize=%s&asc=0", data.bookId, page, pageSize);
        val json = getJson(url, ((Local) arg).config).optJSONObject("chapterlist");
        if (json == null) {
            return 0;
        }
        val book = data.book;
        for (val o : json.getJSONArray("chapters")) {
            val jo = (JSONObject) o;
            val chapter = book.newChapter(jo.getString("chapterName"));
            url = String.format(TEXT_PATTERN, data.bookId, jo.getInt("chapterId"));
            chapter.setText(new CrawlerText(url, this, data.config, chapter));
        }
        if (data.isFirstPage) {
            data.isFirstPage = false;
            return (int) Math.ceil(json.getInt("chapterCount") / json.getDouble("pageSize"));
        }
        return 0;
    }

    @RequiredArgsConstructor
    private static class Local {
        private final Book book;
        private final String url;
        private final String bookId;
        private final TypedConfig config;

        private boolean isFirstPage = true;
    }

    @Override
    public String getText(String url, TypedConfig config) throws JemException, IOException {
        if (!url.contains("http://m.zongheng.com/")) {
            return queryText(getSoup(url, config), "div#readerFs p", System.lineSeparator());
        }
        val sb = new StringBuilder();
        val lines = new LinkedList<String>();
        while (true) {
            JSONObject json = getJson(url, config);
            if (json.getJSONObject("ajaxResult").getInt("code") != 1) {
                break;
            }
            json = json.getJSONObject("result");
            val parts = json.getString("content").split("</p><p>");
            for (int i = 0, end = parts.length; i != end; ++i) {
                String str = parts[i];
                if (str.isEmpty()) {
                    continue;
                }
                if (i == 0) {
                    str = sb.append(str.substring(3)).toString();
                    sb.setLength(0);
                    if (str.isEmpty()) {
                        continue;
                    }
                    lines.add(str);
                } else if (i == end - 2 && !PARAGRAPH_END.contains(str.substring(str.length() - 1))) {
                    sb.append(str);
                } else if (i != end - 1) { // tip in website
                    lines.add(str);
                }
            }
            if (json.getInt("pageCount") == json.getInt("chapterNum")) {
                break;
            }
            url = String.format(TEXT_PATTERN, config.getString("bookId", ""), json.getString("nextChapterId"));
        }
        return join(System.lineSeparator(), lines);
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
