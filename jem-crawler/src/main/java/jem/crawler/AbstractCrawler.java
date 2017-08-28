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

package jem.crawler;

import jclp.io.HttpUtils;
import jclp.io.IOUtils;
import jem.util.TypedConfig;
import jem.util.flob.Flob;
import jem.util.flob.impl.URLFlob;
import lombok.val;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.zip.GZIPInputStream;

import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.StringUtils.valueOfName;

public abstract class AbstractCrawler implements Crawler {
    protected int fetchPage(int page, Object arg) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    protected final void fetchToc(Object arg) throws IOException {
        for (int i = 2; i < fetchPage(1, arg); ++i) {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            fetchPage(i, arg);
        }
    }

    protected final Flob getFlob(String url, TypedConfig config) throws MalformedURLException {
        return new CrawlerFlob(new URL(url), null, "get", config);
    }

    protected final Document getSoup(String url, TypedConfig config) throws IOException {
        return fetchSoup(url, "get", config);
    }

    protected final Document postSoup(String url, TypedConfig config) throws IOException {
        return fetchSoup(url, "post", config);
    }

    private Document fetchSoup(String url, String method, TypedConfig config) throws IOException {
        val tryTimes = config.getInt("crawler.net.tryTimes", 3);
        val timeout = config.getInt("crawler.net.timeout", 5000);
        for (int i = 0; i < tryTimes; ++i) {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            try {
                val conn = Jsoup.connect(url)
                        .userAgent(SoupUtils.randomAgent())
                        .header("Accept-Encoding", "gzip,deflate")
                        .timeout(timeout);
                return "get".equalsIgnoreCase(method) ? conn.get() : conn.post();
            } catch (SocketTimeoutException ignored) {
            }
        }
        throw new SocketTimeoutException("cannot connect to " + url);
    }

    protected final JSONObject getJson(String url, TypedConfig config) throws IOException {
        return fetchJson(url, "get", config);
    }

    protected final JSONObject postJson(String url, TypedConfig config) throws IOException {
        return fetchJson(url, "post", config);
    }

    private JSONObject fetchJson(String url, String method, TypedConfig config) throws IOException {
        val conn = openConnection(url, method, config);
        val contentType = conn.getContentType();
        String encoding = null;
        if (isNotEmpty(contentType)) {
            encoding = valueOfName(contentType, "charset", ";");
        }
        return new JSONObject(IOUtils.toString(openStream(conn), encoding));
    }

    private InputStream openStream(URLConnection conn) throws IOException {
        val encoding = conn.getHeaderField("Content-Encoding");
        return "gzip".equalsIgnoreCase(encoding)
                ? new GZIPInputStream(conn.getInputStream())
                : conn.getInputStream();
    }

    protected final InputStream fetchStream(String url, String method, TypedConfig config) throws IOException {
        return openStream(openConnection(url, method, config));
    }

    protected final URLConnection openConnection(String url, String method, TypedConfig config) throws IOException {
        val tryTimes = config.getInt("crawler.net.tryTimes", 3);
        val timeout = config.getInt("crawler.net.timeout", 5000);
        val request = HttpUtils.Request.builder()
                .url(url)
                .method(method)
                .property("User-Agent", SoupUtils.randomAgent())
                .property("Accept-Encoding", "gzip,deflate")
                .connectTimeout(timeout)
                .build();
        for (int i = 0; i < tryTimes; ++i) {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            try {
                return request.connect();
            } catch (SocketTimeoutException ignored) {
            }
        }
        throw new SocketTimeoutException("cannot connect to " + url);
    }

    private class CrawlerFlob extends URLFlob {
        private final String method;
        private final TypedConfig config;

        CrawlerFlob(URL url, String mime, String method, TypedConfig config) {
            super(url, mime);
            this.method = method;
            this.config = config;
        }

        @Override
        public InputStream openStream() throws IOException {
            return openConnection(getUrl().toString(), method, config).getInputStream();
        }
    }
}
