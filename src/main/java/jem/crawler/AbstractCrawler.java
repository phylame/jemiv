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
import jem.Chapter;
import jem.util.JemException;
import jem.util.TypedConfig;
import lombok.val;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;

import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.StringUtils.valueOfName;

public abstract class AbstractCrawler implements Crawler {
    protected abstract String fetchText(String url, TypedConfig config) throws JemException, IOException;

    @Override
    public String getText(String url, TypedConfig config, Chapter chapter) throws JemException, IOException {
        val text = fetchText(url, config);
        val listener = config.get("crawler.listener", CrawlerListener.class, null);
        if (listener != null) {
            listener.textFetched(chapter, url);
        }
        return text;
    }

    protected int fetchPage(int page) throws IOException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    protected final void fetchToc() throws IOException, InterruptedException {
        for (int i = 2; i < fetchPage(1); ++i) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            fetchPage(i);
        }
    }

    protected final Document getSoup(String url, TypedConfig config) throws IOException, InterruptedException {
        return fetchSoup(url, "get", config);
    }

    protected final Document postSoup(String url, TypedConfig config) throws IOException, InterruptedException {
        return fetchSoup(url, "post", config);
    }

    private Document fetchSoup(String url, String method, TypedConfig config) throws IOException, InterruptedException {
        val tryTimes = config.getInt("crawler.net.tryTimes", 3);
        val timeout = config.getInt("crawler.net.timeout", 5000);
        for (int i = 0; i < tryTimes; ++i) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            try {
                val connection = Jsoup.connect(url)
                        .userAgent(SoupUtils.randomAgent())
                        .header("Accept-Encoding", "gzip,deflate")
                        .timeout(timeout);
                return "get".equalsIgnoreCase(method) ? connection.get() : connection.post();
            } catch (SocketTimeoutException ignored) {
            }
        }
        throw new SocketTimeoutException("cannot connect to " + url);
    }

    protected final JSONObject getJson(String url, TypedConfig config) throws IOException, InterruptedException {
        return fetchJson(url, "get", config);
    }

    protected final JSONObject postJson(String url, TypedConfig config) throws IOException, InterruptedException {
        return fetchJson(url, "post", config);
    }

    private JSONObject fetchJson(String url, String method, TypedConfig config) throws IOException, InterruptedException {
        val connection = openConnection(url, method, config);
        val contentType = connection.getContentType();
        String encoding = null;
        if (isNotEmpty(contentType)) {
            encoding = valueOfName(contentType, "charset", ";");
        }
        return new JSONObject(IOUtils.toString(connection.getInputStream(), encoding));
    }

    protected final URLConnection openConnection(String url, String method, TypedConfig config) throws IOException, InterruptedException {
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
                throw new InterruptedException();
            }
            try {
                return request.connect();
            } catch (SocketTimeoutException ignored) {
            }
        }
        throw new SocketTimeoutException("cannot connect to " + url);
    }
}
