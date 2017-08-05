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
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.concurrent.ExecutorService;

public class CrawlerBook extends Book {
    @Getter
    private int totals;

    public CrawlerBook() {
    }

    public void schedule(@NonNull ExecutorService executor) {
        totals = 0;
        schedule0(this, executor);
    }

    public void schedule0(Chapter book, ExecutorService executor) {
        val text = book.getText();
        if (text instanceof CrawlerText) {
            ((CrawlerText) text).schedule(executor);
            ++totals;
        }
        for (val chapter : book) {
            schedule0(chapter, executor);
        }
    }
}
