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

import jclp.setting.Settings;
import jclp.util.ServiceManager;
import jem.Book;
import jem.util.JemException;
import jem.util.TypedConfig;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.net.URL;

public class CrawlerManager extends ServiceManager<CrawlerFactory> {
    public CrawlerManager() {
        super(CrawlerFactory.class);
    }

    public CrawlerManager(ClassLoader loader) {
        super(CrawlerFactory.class, loader);
    }

    public Crawler getCrawler(String host) {
        val factory = getService(host);
        return factory != null ? factory.getCrawler() : null;
    }

    public Book fetchBook(@NonNull String url, Settings settings) throws IOException, JemException {
        val crawler = getCrawler(new URL(url).getHost());
        return crawler != null
                ? crawler.getBook(url, new TypedConfig(settings))
                : null;
    }
}
