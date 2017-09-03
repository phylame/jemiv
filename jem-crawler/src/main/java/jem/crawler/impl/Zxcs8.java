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

package jem.crawler.impl;

import jclp.io.IOUtils;
import jclp.setting.Settings;
import jclp.text.Render;
import jclp.util.CollectionUtils;
import jclp.util.StringUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jem.Book;
import jem.Chapter;
import jem.crawler.M;
import jem.crawler.SoupUtils;
import jem.epm.Parser;
import jem.epm.impl.AbstractFactory;
import jem.epm.impl.FileParser;
import jem.epm.util.ParserException;
import jem.util.JemException;
import jem.util.flob.Flob;
import jem.util.flob.Flobs;
import jem.util.text.AbstractText;
import jem.util.text.Texts;
import lombok.val;
import org.jsoup.Jsoup;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static jclp.util.StringUtils.trimmed;
import static jem.Attributes.*;

public class Zxcs8 extends AbstractFactory implements FileParser {
    private static final String ENCODING = "GBK";
    private static final String PREFIX = "document.write ('";
    private static final String SUFFIX = "')";

    @Override
    public Book parse(String input, Settings arguments) throws IOException, JemException {
        val base = Paths.get(input);
        if (!Files.isDirectory(base)) {
            throw new ParserException(M.translator().tr("err.zxcs8.notDir", base));
        }
        val path = base.resolve("js/page.js");
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }
        val engine = new ScriptEngineManager().getEngineByName("js");
        try (val stream = Files.newInputStream(path); val reader = IOUtils.readerFor(stream, ENCODING)) {
            engine.eval(reader);
        } catch (ScriptException e) {
            throw new JemException(e);
        }
        val book = new Book("Untitled");
        parse(book, path.getParent(), engine);
        return book;
    }

    private void parse(Book book, Path base, ScriptEngine engine) throws IOException, JemException {
        ScriptObjectMirror o = (ScriptObjectMirror) engine.get("pages");
        if (o == null || !o.isArray()) {
            throw new ParserException(M.translator().tr("err.zxcs8.noPages"));
        }
        int i = 0;
        Chapter section = null;
        for (val value : o.values()) {
            ++i;
            if (!(value instanceof ScriptObjectMirror)) {
                throw new ParserException(M.translator().tr("err.zxcs8.badPages"));
            }
            o = (ScriptObjectMirror) value;
            if (!o.isArray()) {
                throw new ParserException(M.translator().tr("err.zxcs8.badPages"));
            }
            val values = (List<Object>) o.values();
            val size = values.size();
            if (size < 3) {
                throw new ParserException(M.translator().tr("err.zxcs8.badPages"));
            }
            if (size == 4 && i == 1) {
                val doc = Jsoup.parse(values.get(1).toString());
                setIntro(book, SoupUtils.joinText(doc.select("body"), System.lineSeparator()));
                setCover(book, getCover(base, values.get(3).toString()));
                continue;
            }
            if (size > 3) {
                if (section == null) {
                    section = book.newChapter(values.get(3).toString());
                } else {
                    section = section.getParent().newChapter(values.get(3).toString());
                }
            }
            Chapter chapter;
            if (section == null) {
                chapter = book.newChapter(values.get(1).toString());
            } else {
                chapter = section.newChapter(values.get(1).toString());
            }
            if (size > 6) {
                setCover(section, getCover(base, values.get(6).toString()));
            }
            setWords(chapter, values.get(2).toString());
            chapter.setText(new MyText(base.resolve("../txt/" + values.get(0) + ".txt").normalize()));
        }
        o = (ScriptObjectMirror) engine.get("hangxing");
        if (o == null || !o.isArray()) {
            return;
        }
        List<Object> values = (List<Object>) o.values();
        if (values.isEmpty()) {
            return;
        }
        Object value = values.get(0);
        if (!(value instanceof ScriptObjectMirror)) {
            return;
        }
        o = (ScriptObjectMirror) value;
        if (!o.isArray()) {
            return;
        }
        values = (List<Object>) o.values();
        val size = values.size();
        if (size > 0) {
            setTitle(book, values.get(0).toString());
        }
        if (size > 1) {
            setAuthor(book, values.get(1).toString());
        }
        if (size > 2) {
            val lines = values.get(2).toString().split("<Br>");
            setIntro(book, StringUtils.join(System.lineSeparator(), lines, new Render<String>() {
                @Override
                public String render(String str) {
                    return trimmed(str);
                }
            }));
        }
    }

    private Flob getCover(Path base, String html) {
        val src = Jsoup.parse(html).select("img").attr("src");
        return Flobs.forFile(base.resolve(src).normalize().toFile());
    }

    @Override
    public Book parse(File dir, Settings arguments) throws IOException, JemException {
        return parse(dir.getPath(), arguments);
    }

    @Override
    public String getName() {
        return "CHM for zxcs8.com";
    }

    @Override
    public Set<String> getNames() {
        return CollectionUtils.setOf("zxcs8");
    }

    @Override
    public Parser getParser() {
        return this;
    }

    private static class MyText extends AbstractText {
        private final Path path;

        MyText(Path path) {
            super(Texts.PLAIN);
            this.path = path;
        }

        @Override
        public Iterator<String> iterator() {
            try (val reader = new LineNumberReader(IOUtils.readerFor(path.toFile(), ENCODING))) {
                String line;
                val list = new LinkedList<String>();
                while ((line = reader.readLine()) != null) {
                    if (reader.getLineNumber() == 1 || line.isEmpty()) {
                        continue;
                    }
                    line = line.substring(PREFIX.length(), line.length() - SUFFIX.length());
                    for (val s : line.split("<p>")) {
                        list.add(trimmed(s));
                    }
                }
                return list.iterator();
            } catch (IOException e) {
                return Collections.emptyIterator();
            }
        }

        @Override
        public String toString() {
            return StringUtils.join(System.lineSeparator(), iterator());
        }

        @Override
        public long writeTo(Writer writer) throws IOException {
            long chars = 0;
            val separator = System.lineSeparator();
            for (String line : this) {
                writer.append(line).append(separator);
                chars += line.length() + separator.length();
            }
            writer.flush();
            return chars;
        }
    }
}
