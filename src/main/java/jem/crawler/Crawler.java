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

import jem.Book;
import jem.Chapter;
import jem.util.JemException;
import jem.util.TypedConfig;

import java.io.IOException;

public interface Crawler {
    /**
     * Fetches a book from specified URL.
     *
     * @param url    the URL to be fetched
     * @param config config for fetching book
     * @return the fetched book
     * @throws JemException if an error occurs when fetching
     * @throws IOException  if an I/O error occurs
     */
    Book getBook(String url, TypedConfig config) throws JemException, IOException;

    /**
     * Fetches text of chapter from specified URL.
     * <p>
     * This method will be invoked by {@code CrawlerText} when getting text.
     * </p>
     *
     * @param url     the URL to be fetched
     * @param config  config for fetching text
     * @param chapter the chapter containing this text
     * @return the text string of chapter
     * @throws JemException if an error occurs when fetching
     * @throws IOException  if an I/O error occurs
     * @see jem.crawler.CrawlerText
     */
    String getText(String url, TypedConfig config, Chapter chapter) throws JemException, IOException;
}
