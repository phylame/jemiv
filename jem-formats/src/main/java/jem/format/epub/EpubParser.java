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

package jem.format.epub;

import jclp.io.PathUtils;
import jclp.log.Log;
import jclp.util.DateUtils;
import jclp.util.MiscUtils;
import jclp.vdm.VdmReader;
import jem.Attributes;
import jem.Book;
import jem.Chapter;
import jem.epm.impl.VdmParser;
import jem.epm.util.ParserException;
import jem.epm.util.VdmUtils;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.flob.Flobs;
import jem.util.text.Texts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static jclp.util.StringUtils.isEmpty;
import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.StringUtils.trimmed;
import static jem.format.util.ParserUtils.error;
import static jem.format.util.ParserUtils.getAttribute;

/**
 * Epm parser for ePub.
 */
public class EpubParser extends VdmParser implements EPUB {
    private static final String TAG = "EpubParser";

    @Override
    protected Book parse(VdmReader input, TypedConfig config) throws IOException, JemException {
        if (!validMT(input)) {
            throw error("epub.parse.invalidMT", MIME_PATH, MIME_EPUB);
        }
        val data = new Local(new Book(), input);
        readContainer(data);
        readOpf(data);
        readNcx(data);
        return data.book;
    }

    private boolean validMT(VdmReader input) throws IOException {
        return MIME_EPUB.equals(VdmUtils.getText(input, MIME_PATH, "ASCII").trim());
    }

    private void readContainer(Local data) throws ParserException, IOException {
        val xpp = data.newXpp();
        try (val stream = VdmUtils.getStream(data.reader, CONTAINER_PATH)) {
            xpp.setInput(stream, null);
            int event = xpp.getEventType();
            do {
                switch (event) {
                case XmlPullParser.START_TAG: {
                    if (xpp.getName().equals("rootfile")) {
                        val mime = getAttribute(xpp, "media-type");
                        if (mime.equals(MIME_OPF)) {
                            data.opfPath = getAttribute(xpp, "full-path");
                            break;
                        }
                    }
                }
                break;
                }
                event = xpp.next();
            } while (event != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw error(e, "epub.parse.invalidContainer", e.getLocalizedMessage());
        }
        data.opsDir = PathUtils.dirName(data.opfPath);
    }

    private void readOpf(Local data) throws ParserException, IOException {
        if (isEmpty(data.opfPath)) {
            throw error("epub.parse.noOpf");
        }
        String coverId = null;
        val sb = new StringBuilder();
        val xpp = data.newXpp();
        try (val stream = VdmUtils.getStream(data.reader, data.opfPath)) {
            xpp.setInput(stream, null);
            boolean hasText = false;
            int event = xpp.getEventType();
            do {
                val tag = xpp.getName();
                switch (event) {
                case XmlPullParser.START_TAG: {
                    hasText = false;
                    if (tag.equals("item")) {
                        data.items.put(getAttribute(xpp, "id"),
                                new Item(getAttribute(xpp, "href"), getAttribute(xpp, "media-type")));
                    } else if (tag.equals("itemref")) {
                        // ignored
                    } else if (tag.equals("reference")) {
                        // ignored
                    } else if (tag.equals("meta")) {
                        val name = xpp.getAttributeValue(null, "name");
                        val value = xpp.getAttributeValue(null, "content");
                        if ("cover".equals(name)) {
                            coverId = value;
                        } else if (value != null) {
                            data.book.getAttributes().set(name, value);
                        }
                    } else if (tag.startsWith("dc:")) {
                        data.scheme = xpp.getAttributeValue(null, "opf:scheme");
                        data.role = xpp.getAttributeValue(null, "opf:role");
                        data.event = xpp.getAttributeValue(null, "opf:event");
                        hasText = true;
                    } else if (tag.equals("package")) {
                        val version = getAttribute(xpp, "version");
                        if (!version.startsWith("2")) {
                            throw error("epub.parse.unsupportedVersion", version);
                        }
                    } else if (tag.equals("spine")) {
                        data.tocId = xpp.getAttributeValue(null, "toc");
                    }
                }
                break;
                case XmlPullParser.TEXT: {
                    if (hasText) {
                        sb.append(xpp.getText());
                    }
                }
                break;
                case XmlPullParser.END_TAG: {
                    if (tag.startsWith("dc:")) {
                        parseMetadata(tag.substring(3), xpp, sb, data);
                    }
                    sb.setLength(0);
                }
                break;
                }
                event = xpp.next();
            } while (event != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw error(e, "epub.parse.invalidOpf", e.getLocalizedMessage());
        }
        if (isNotEmpty(coverId)) {
            val item = data.items.remove(coverId);
            if (item != null) {
                Attributes.setCover(data.book, Flobs.forVdm(data.reader, data.opsDir + '/' + item.href, null));
            }
        }
    }

    private void parseMetadata(String name, XmlPullParser xpp, StringBuilder b, Local data) {
        val book = data.book;
        val text = trimmed(b.toString());
        Object value = text;
        switch (name) {
        case "identifier": {
            if ("uuid".equals(data.scheme)) {
                name = "uuid";
            } else if ("isbn".equals(data.scheme)) {
                name = Attributes.ISBN;
            }
        }
        break;
        case "creator": {
            if (data.role == null) {
                name = Attributes.AUTHOR;
            } else if (data.role.equals("aut")) {
                name = Attributes.AUTHOR;
            }
        }
        break;
        case "date": {
            if (data.event == null) {
                name = Attributes.PUBDATE;
            } else if (data.event.equals("creation")) {
                name = Attributes.PUBDATE;
            } else if (data.event.equals("modification")) {
                name = Attributes.DATE;
            }
            try {
                value = DateUtils.parse(text, "yyyy-m-D");
            } catch (ParseException e) {
                Log.d(TAG, "invalid date format", e);
                return;
            }
        }
        break;
        case "contributor": {
            if (data.role == null) {
                name = Attributes.VENDOR;
            } else if (data.role.equals("bkp")) {
                name = Attributes.VENDOR;
            }
        }
        break;
        case "type":
        case "subject": {
            name = Attributes.GENRE;
        }
        break;
        case "description": {
            name = Attributes.INTRO;
            value = Texts.forString(text, Texts.PLAIN);
        }
        break;
        case "language": {
            value = MiscUtils.parseLocale(text);
        }
        break;
        }
        book.getAttributes().set(name, value);
    }

    private void readNcx(Local data) throws ParserException, IOException {
        if (isEmpty(data.tocId)) {
            Log.d(TAG, "no toc resource found");
            return;
        }
        Item item = data.items.remove(data.tocId);
        if (item == null) {
            Log.d(TAG, "no toc resource found for id: {0}", data.tocId);
            return;
        }
        val book = data.book;
        val b = new StringBuilder();
        val xpp = data.newXpp();
        try (val in = VdmUtils.getStream(data.reader, data.opsDir + '/' + item.href)) {
            xpp.setInput(in, null);
            boolean hasText = false;
            boolean forChapter = false;
            Chapter chapter = book;
            int event = xpp.getEventType();
            do {
                val tag = xpp.getName();
                switch (event) {
                case XmlPullParser.START_TAG: {
                    hasText = false;
                    if (tag.equals("navPoint")) {
                        val id = getAttribute(xpp, "id");
                        item = data.items.remove(id);
                        if (item == null) {
                            Log.d(TAG, "no such resource with id: {0}", id);
                        }
                        val sub = new Chapter();
                        chapter.append(sub);
                        chapter = sub;
                    } else if (tag.equals("content")) {
                        val href = data.opsDir + '/' + getAttribute(xpp, "src");
                        String mime, type;
                        if (item == null) {
                            mime = "text/plain";
                            type = Texts.PLAIN;
                        } else {
                            mime = item.mime;
                            type = mime.contains("html") ? Texts.HTML : Texts.PLAIN;
                        }
                        chapter.setText(Texts.forFlob(Flobs.forVdm(data.reader, href, mime), null, type));
                    } else if (tag.equals("text")) {
                        hasText = true;
                    } else if (tag.equals("meta")) {
                        book.getExtensions().set(getAttribute(xpp, "name"), getAttribute(xpp, "content"));
                    } else if (tag.equals("navLabel")) {
                        forChapter = true;
                    }
                }
                break;
                case XmlPullParser.TEXT: {
                    if (hasText) {
                        b.append(xpp.getText());
                    }
                }
                break;
                case XmlPullParser.END_TAG: {
                    if (tag.equals("navPoint")) {
                        chapter = chapter.getParent();
                    } else if (tag.equals("text")) {
                        if (forChapter) {
                            Attributes.setTitle(chapter, trimmed(b.toString()));
                        }
                    }
                    b.setLength(0);
                }
                break;
                }
                event = xpp.next();
            } while (event != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw error(e, "epub.parse.invalidNcx", e.getLocalizedMessage());
        }
        val extensions = book.getExtensions();
        for (val e : data.items.entrySet()) {
            item = e.getValue();
            extensions.set("opf-" + e.getKey(), Flobs.forVdm(data.reader, data.opsDir + '/' + item.href, item.mime));
        }
    }

    @RequiredArgsConstructor
    private static class Local {
        final Book book;
        final VdmReader reader;
        XmlPullParser xpp;
        String opfPath;

        String opsDir;

        // OPF scheme
        String scheme;

        // OPF role
        String role;

        // OPF event
        String event;

        String tocId;

        Map<String, Item> items = new HashMap<>();

        @SneakyThrows(XmlPullParserException.class)
        private XmlPullParser newXpp() {
            val factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xpp = factory.newPullParser();
            return xpp;
        }
    }

    @RequiredArgsConstructor
    private static class Item {
        final String href;
        final String mime;
    }

}
