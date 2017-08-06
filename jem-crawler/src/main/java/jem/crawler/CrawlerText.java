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

import jclp.log.Level;
import jclp.log.Log;
import jclp.util.AsyncTask;
import jem.Chapter;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.text.AbstractText;
import jem.util.text.Texts;
import lombok.NonNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class CrawlerText extends AbstractText {
    private static final String TAG = "CrawlerText";

    private final String url;
    private final Crawler crawler;
    private final Chapter chapter;
    private final TypedConfig config;
    private final AsyncTask<String> task = new AsyncTask<String>() {
        @Override
        protected String handleGet() throws JemException, IOException {
            return crawler.getText(url, config, chapter);
        }
    };

    public CrawlerText(String url, @NonNull Crawler crawler, TypedConfig config, Chapter chapter) {
        super(Texts.PLAIN);
        this.url = url;
        this.crawler = crawler;
        this.chapter = chapter;
        this.config = config;
    }

    void schedule(@NonNull ExecutorService executor) {
        task.schedule(executor);
    }

    @Override
    public String toString() {
        try {
            return task.get();
        } catch (Exception e) {
            if (Log.isEnable(Level.DEBUG)) {
                Log.d(TAG, "cannot fetch text from " + url, e);
            }
            return "";
        }
    }
}
