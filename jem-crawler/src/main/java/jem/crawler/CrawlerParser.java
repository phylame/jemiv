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

import static jclp.util.CollectionUtils.setOf;

import java.io.IOException;
import java.util.Set;

import jclp.setting.Settings;
import jem.Book;
import jem.epm.Parser;
import jem.epm.impl.AbstractFactory;
import jem.epm.util.ParserException;
import jem.util.JemException;
import lombok.val;

public class CrawlerParser extends AbstractFactory implements Parser {
    @Override
    public Book parse(String input, Settings arguments) throws IOException, JemException {
        CrawlerManager crawlerManager = null;
        if (arguments != null) {
            val value = arguments.get("crawler.crawlerManager");
            if (value instanceof CrawlerManager) {
                crawlerManager = (CrawlerManager) value;
            }
        }
        if (crawlerManager == null) {
            crawlerManager = new CrawlerManager();
        }
        val book = crawlerManager.fetchBook(input, arguments);
        if (book == null) {
            throw new ParserException(M.translator().tr("error.crawler.unsupported", input));
        }
        return book;
    }

    @Override
    public String getName() {
        return "Book Crawler";
    }

    @Override
    public Set<String> getNames() {
        return setOf("crawler");
    }

    @Override
    public boolean hasParser() {
        return true;
    }

    @Override
    public Parser getParser() {
        return this;
    }
}
