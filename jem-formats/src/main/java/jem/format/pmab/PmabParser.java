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

import static jclp.util.StringUtils.firstPartOf;
import static jclp.util.StringUtils.isEmpty;
import static jclp.util.StringUtils.secondPartOf;
import static jclp.util.StringUtils.valueOfName;
import static jem.epm.util.VdmUtils.getStream;
import static jem.format.util.ParserUtils.error;
import static jem.format.util.ParserUtils.parseDate;
import static jem.format.util.ParserUtils.parseDouble;
import static jem.format.util.ParserUtils.parseInteger;
import static jem.format.util.ParserUtils.parseLocale;
import static jem.format.util.ParserUtils.requiredAttribute;
import static jem.util.Variants.BOOLEAN;
import static jem.util.Variants.DATETIME;
import static jem.util.Variants.INTEGER;
import static jem.util.Variants.LOCALE;
import static jem.util.Variants.REAL;
import static jem.util.Variants.STRING;
import static jem.util.Variants.TEXT;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import jclp.vdm.VdmReader;
import jem.Attributes;
import jem.Book;
import jem.Chapter;
import jem.epm.impl.VdmParser;
import jem.epm.util.ParserException;
import jem.epm.util.VdmUtils;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.VariantMap;
import jem.util.flob.Flobs;
import jem.util.text.Text;
import jem.util.text.Texts;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Epm parser for PMAB.
 */
public class PmabParser extends VdmParser implements PMAB {
    @Override
    protected Book parse(VdmReader input, TypedConfig config) throws IOException, JemException {
        if (!validMT(input)) {
            throw error("pmab.parse.invalidMT", MIME_PATH, MIME_PMAB);
        }
        val data = new Local(new Book(), input, config);
        readPbm(data);
        readPbc(data);
        return data.book;
    }

    private boolean validMT(VdmReader input) throws IOException {
        return MIME_PMAB.equals(VdmUtils.getText(input, MIME_PATH, "ASCII").trim());
    }

    private void readPbm(Local data) throws ParserException, IOException {
        int version = 0;
        val xpp = data.newXpp();
        boolean hasText = false;
        val sb = new StringBuilder();
        try (val stream = getStream(data.reader, PBM_PATH)) {
            xpp.setInput(stream, null);
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
                        sb.append(xpp.getText());
                    }
                }
                break;
                case XmlPullParser.END_TAG: {
                    endPbm(xpp.getName(), sb, data);
                    sb.setLength(0);
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
            data.values = data.book.getAttributes();
        break;
        case "extensions":
            data.values = data.book.getExtensions();
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

    private void endPbm(String tag, StringBuilder sb, Local data) throws ParserException, IOException {
        if (tag.equals("item")) {
            data.values.set(data.itemName, parseVariant(sb.toString().trim(), data));
        }
    }

    private void readPbc(Local data) throws IOException, JemException {
        int version = 0;
        val xpp = data.newXpp();
        boolean hasText = false;
        val sb = new StringBuilder();
        try (val stream = getStream(data.reader, PBC_PATH)) {
            xpp.setInput(stream, null);
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
                        sb.append(xpp.getText());
                    }
                }
                break;
                case XmlPullParser.START_DOCUMENT: {
                    data.chapter = data.book;
                }
                break;
                case XmlPullParser.END_TAG: {
                    endPbc(xpp.getName(), sb, data);
                    sb.setLength(0);
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

    private void endPbc(String tag, StringBuilder sb, Local data) throws IOException, ParserException {
        val itemType = data.itemType;
        switch (tag) {
        case "chapter":
            data.chapter = data.chapter.getParent();
        break;
        case "item": {
            data.chapter.getAttributes().set(data.itemName, parseVariant(sb.toString().trim(), data));
        }
        break;
        case "content": {
            Text text;
            val str = sb.toString().trim();
            if (itemType.startsWith("text/")) {
                val flob = Flobs.forVdm(data.reader, str, firstPartOf(itemType, ";"));
                val encoding = valueOfName(itemType, "encoding", ";");
                text = Texts.forFlob(flob, data.config.getString("pmab.textEncoding", encoding),
                        secondPartOf(flob.getMime(), "/"));
            } else {
                text = Texts.forString(str);
            }
            data.chapter.setText(text);
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

    private class Local {
        private Book book;
        private VdmReader reader;
        private TypedConfig config;
        private XmlPullParser xpp;

        private Local(Book book, VdmReader reader, TypedConfig config) {
            this.book = book;
            this.reader = reader;
            this.config = config;
        }

        private String itemName, itemType; // item attribute
        private VariantMap values;

        private Chapter chapter;

        private Map<String, Object> metadata;

        private void newChapter() {
            this.chapter = chapter.newChapter("");
        }

        @SneakyThrows(XmlPullParserException.class)
        private XmlPullParser newXpp() {
            val factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            xpp = factory.newPullParser();
            return xpp;
        }
    }
}
