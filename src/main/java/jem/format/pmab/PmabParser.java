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

package jem.format.pmab;

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
import jem.util.text.Text;
import jem.util.text.Texts;
import lombok.SneakyThrows;
import lombok.val;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static jclp.util.StringUtils.*;
import static jem.epm.util.VdmUtils.getStream;
import static jem.format.util.ParserUtils.*;
import static jem.util.Variants.*;

/**
 * Epm parser for PMAB.
 */
public class PmabParser extends VdmParser implements PMAB {
    @Override
    protected Book parse(VdmReader input, TypedConfig config) throws IOException, JemException {
        if (!PMAB.MIME_PMAB.equals(VdmUtils.getText(input, PMAB.MIME_PATH, "ASCII").trim())) {
            throw error("pmab.parse.invalidMT", PMAB.MIME_PATH, PMAB.MIME_PMAB);
        }
        val data = new Local(new Book(), input, config);
        readPbm(data);
        readPbc(data);
        return data.book;
    }

    private void readPbm(Local data) throws ParserException, IOException {
        int version = 0;
        val xpp = data.xpp;
        boolean hasText = false;
        val b = new StringBuilder();
        try (val in = getStream(data.reader, PBM_PATH)) {
            xpp.setInput(in, null);
            int event = xpp.getEventType();
            do {
                switch (event) {
                    case XmlPullParser.START_TAG: {
                        val tag = xpp.getName();
                        if (version != 0) {
                            hasText = startPbm(tag, data);
                        } else if (tag.equals("pbm")) {
                            version = getVersion(xpp, "pmab.parse.unsupportedPBM");
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
                        endPbm(xpp.getName(), b, data);
                        b.setLength(0);
                    }
                    break;
                }
                event = xpp.next();
            } while (event != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw error(e, "pmab.parse.invalidPBM");
        }
    }

    private boolean startPbm(String tag, Local data) throws ParserException {
        val xpp = data.xpp;
        boolean hasText = false;
        switch (tag) {
            case "item": {
                data.itemName = requiredAttribute(xpp, "name");
                data.itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
            }
            break;
            case "attributes":
                data.inAttributes = true;
                break;
            case "meta":
                data.metadata.put(requiredAttribute(xpp, "name"), requiredAttribute(xpp, "value"));
                break;
            case "head":
                data.metadata = new HashMap<>();
                break;
        }
        return hasText;
    }

    private void endPbm(String tag, StringBuilder b, Local data) throws ParserException, IOException {
        if (tag.equals("item")) {
            val value = parseVariant(b.toString().trim(), data);
            if (data.inAttributes) {
                data.book.getAttributes().set(data.itemName, value);
            } else {
                data.book.getExtensions().set(data.itemName, value);
            }
        } else if (tag.equals("attributes")) {
            data.inAttributes = false;
        }
    }

    private void readPbc(Local data) throws IOException, JemException {
        int version = 0;
        val xpp = data.xpp;
        boolean hasText = false;
        val b = new StringBuilder();
        try (val in = getStream(data.reader, PBM_PATH)) {
            xpp.setInput(in, null);
            int event = xpp.getEventType();
            do {
                switch (event) {
                    case XmlPullParser.START_TAG: {
                        val tag = xpp.getName();
                        if (version != 0) {
                            hasText = startPbc(tag, data);
                        } else if (tag.equals("pbc")) {
                            version = getVersion(xpp, "pmab.parse.unsupportedPBC");
                        }
                    }
                    break;
                    case XmlPullParser.TEXT: {
                        if (hasText) {
                            b.append(xpp.getText());
                        }
                    }
                    break;
                    case XmlPullParser.START_DOCUMENT: {
                        data.currentChapter = data.book;
                    }
                    break;
                    case XmlPullParser.END_TAG: {
                        endPbc(xpp.getName(), b, data);
                        b.setLength(0);
                    }
                    break;
                }
                event = xpp.next();
            } while (event != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw error(e, "pmab.parse.invalidPBC");
        }
    }

    private boolean startPbc(String tag, Local data) throws ParserException {
        val xpp = data.xpp;
        boolean hasText = false;
        switch (tag) {
            case "chapter":
                data.newChapter();
                break;
            case "item": {
                data.itemName = requiredAttribute(xpp, "name");
                data.itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
            }
            break;
            case "content": {
                data.itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
            }
            break;
        }
        return hasText;
    }

    private void endPbc(String tag, StringBuilder b, Local data) throws IOException, ParserException {
        val itemType = data.itemType;
        switch (tag) {
            case "chapter":
                data.currentChapter = data.currentChapter.getParent();
                break;
            case "item": {
                String text = b.toString().trim();
                data.currentChapter.getAttributes().set(data.itemName, parseVariant(text, data));
            }
            break;
            case "content": {
                Text text;
                val data = b.toString().trim();
                if (isEmpty(itemType)) {
                    text = Texts.forString(data, Texts.PLAIN);
                } else if (itemType.startsWith("text/")) {
                    val flob = Flobs.forZip(data.zip, data, firstPartOf(itemType, ";"));
                    val encoding = valueOfName(itemType, "encoding", ";");
                    text = Texts.forFlob(flob, isNotEmpty(encoding) ? encoding : data.config.textEncoding, Texts.PLAIN);
                } else {
                    text = Texts.forString(data, Texts.PLAIN);
                }
                data.currentChapter.setText(text);
            }
            break;
        }
    }

    private int getVersion(XmlPullParser xpp, String error) throws ParserException {
        val str = requiredAttribute(xpp, "version");
        switch (str) {
            case "3.0":
                return 3;
            default:
                throw error(error);
        }
    }

    //
    private Object detectValue(String text, String name) throws ParserException {
        val type = Attributes.getType(name);
        if (type == null) {
            return text;
        }
        switch (type) {
            case TEXT:
                return Texts.forString(text);
            case LOCALE:
                return parseLocale(text);
            default:
                return text;
        }
    }

    private Object parseVariant(String text, Local data) throws IOException, ParserException {
        val itemType = data.itemType;
        Object value;
        if (isEmpty(itemType)) { // no type specified, text as string
            value = detectValue(text, data.itemName);
        } else {
            val type = firstPartOf(itemType, ";");
            if (type.equals(STRING)) {
                value = detectValue(text, data.itemName);
            } else if (type.equals(DATETIME) || type.equals("date") || type.equals("time")) {
                val format = valueOfName(itemType, "format", ";");
                value = parseDate(text, data.config.getString("pmab.dateFormat", format));
            } else if (type.startsWith("text/")) { // text object
                val mime = type.substring(5);
                val flob = Flobs.forVdm(data.reader, text, "text/" + mime);
                val encoding = valueOfName(itemType, "encoding", ";");
                value = Texts.forFlob(flob, data.config.getString("pmab.textEncoding", encoding), mime);
            } else if (type.equals(LOCALE)) {
                value = parseLocale(text);
            } else if (type.matches("[\\w]+/[\\w\\-]+")) { // file object
                value = Flobs.forVdm(data.reader, text, type);
            } else if (type.equals(INTEGER) || type.equals("uint")) {
                value = parseInteger(text);
            } else if (type.equals(REAL)) {
                value = parseDouble(text);
            } else if (type.equals(BOOLEAN)) {
                value = Boolean.parseBoolean(text);
            } else { // parsed as string
                value = detectValue(text, data.itemName);
            }
        }
        return value;
    }

//
//    private boolean startPBCv2(String tag, data data) throws IOException, ParserException {
//        val xpp = data.xpp;
//        boolean hasText = false;
//        switch (tag) {
//        case "chapter": {
//            val href = xpp.getAttributeValue(null, "href");
//            if (isEmpty(href)) {
//                data.newChapter();
//            } else {
//                val flob = Flobs.forZip(data.zip, href, "text/plain");
//                data.chapterEncoding = xpp.getAttributeValue(null, "encoding");
//                if (isEmpty(data.chapterEncoding)) {
//                    data.chapterEncoding = data.config.textEncoding;
//                }
//                data.newChapter();
//                data.currentChapter.setText(Texts.forFlob(flob, data.chapterEncoding, Texts.PLAIN));
//            }
//        }
//        break;
//        case "title":
//            hasText = true;
//        break;
//        case "cover": {
//            val href = requiredAttribute(xpp, "href");
//            val mime = requiredAttribute(xpp, "media-type");
//            Attributes.setCover(data.currentChapter, Flobs.forZip(data.zip, href, mime));
//        }
//        break;
//        case "intro": {
//            val flob = Flobs.forZip(data.zip, requiredAttribute(xpp, "href"), "text/plain");
//            String encoding = xpp.getAttributeValue(null, "encoding");
//            if (isEmpty(encoding)) {
//                encoding = data.config.useChapterEncoding ? data.chapterEncoding : data.config.textEncoding;
//            }
//            Attributes.setIntro(data.currentChapter, Texts.forFlob(flob, encoding, Texts.PLAIN));
//        }
//        break;
//        }
//        return hasText;
//    }
//
//    private void endPBCv2(String tag, StringBuilder b, data data) {
//        if (tag.equals("chapter")) {
//            data.currentChapter = data.currentChapter.getParent();
//        } else if (tag.equals("title")) {
//            Attributes.setTitle(data.currentChapter, b.toString().trim());
//        }
//    }

    private class Local {
        private Book book;
        private VdmReader reader;
        private TypedConfig config;
        private XmlPullParser xpp;

        @SneakyThrows(XmlPullParserException.class)
        private Local(Book book, VdmReader reader, TypedConfig config) {
            this.book = book;
            this.reader = reader;
            this.config = config;
            val factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xpp = factory.newPullParser();
        }

        // PBM 3 data
        private String itemName, itemType; // item attribute
        private boolean inAttributes = false; // item is contained in <attributes>
        // PMAB 2 counter
        private int count, order;
        // PBM 2 data
        private String attrName, mediaType;
        // pbc data
        private Chapter currentChapter;
        // used for encoding of intro in chapter
        private String chapterEncoding;

        private Map<String, Object> metadata;

        private boolean checkCount() {
            return count < 0 || order < count;
        }

        private void newChapter() {
            val chapter = new Chapter();
            currentChapter.append(chapter);
            currentChapter = chapter;
        }
    }
}
