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

import jclp.io.PathUtils;
import jclp.util.DateUtils;
import jclp.util.MiscUtils;
import jclp.vdm.VdmWriter;
import jem.Book;
import jem.Chapter;
import jem.epm.impl.VdmMaker;
import jem.format.util.xml.XmlRender;
import jem.util.JemException;
import jem.util.TypedConfig;
import jem.util.VariantMap;
import jem.util.Variants;
import jem.util.flob.Flob;
import jem.util.text.Text;
import jem.util.text.Texts;
import lombok.SneakyThrows;
import lombok.val;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static jem.epm.util.VdmUtils.write;

/**
 * Epm maker for PMAB.
 */
public class PmabMaker extends VdmMaker implements PMAB {
    @Override
    protected void make(Book book, VdmWriter writer, TypedConfig config) throws IOException, JemException {
        writeMime(writer);
        val data = new Local(book, writer, config);
        writePbm(data);
        writePbc(data);
    }

    private void writeMime(VdmWriter writer) throws IOException {
        write(writer, MIME_PATH, MIME_PMAB, "ASCII");
    }

    private void writePbm(Local data) throws IOException {
        val buffer = initXml("pbm", PBM_XMLNS, data);
        writeHeader(data);
        writeVariants(data.book.getAttributes(), "attributes", "", data);
        writeVariants(data.book.getExtensions(), "extensions", "", data);
        writeXml(buffer, PBM_PATH, data);
    }

    private void writeHeader(Local data) throws IOException {
        val values = (Map<?, ?>) data.config.get("pmab.meta", Map.class, null);
        if (values != null && !values.isEmpty()) {
            val render = data.render.beginTag("head");
            for (val entry : values.entrySet()) {
                render.beginTag("meta")
                        .attribute("name", entry.getKey().toString())
                        .attribute("value", entry.getValue().toString())
                        .endTag();
            }
            render.endTag();
        }
    }

    private void writePbc(Local data) throws JemException, IOException {
        val buffer = initXml("pbc", PBC_XML_NS, data);
        val render = data.render.beginTag("nav");
        int i = 1;
        for (val chapter : data.book) {
            if (Thread.interrupted()) {
                throw new JemException("interrupted");
            }
            writeChapter(chapter, Integer.toString(i++), data);
        }
        render.endTag();
        writeXml(buffer, PBC_PATH, data);
    }

    private void writeChapter(Chapter chapter, String suffix, Local data) throws JemException, IOException {
        val render = data.render.beginTag("chapter");
        val prefix = "chapter-" + suffix;

        writeVariants(chapter.getAttributes(), "attributes", prefix + "-", data);

        val text = chapter.getText();
        if (text != null) {
            val path = writeText(text, prefix, data);
            render.beginTag("content")
                    .attribute("type", getType(text, data))
                    .text(path)
                    .endTag();
        }

        int i = 1;
        for (val child : chapter) {
            if (Thread.interrupted()) {
                throw new JemException("interrupted");
            }
            writeChapter(child, suffix + "-" + (i++), data);
        }
        render.endTag();
    }

    private StringWriter initXml(String root, String xmlns, Local data) throws IOException {
        val writer = new StringWriter();
        data.render.output(writer)
                .beginXml()
                .docdecl(root)
                .beginTag(root)
                .attribute("version", "3.0")
                .xmlns(xmlns);
        return writer;
    }

    private void writeXml(StringWriter writer, String path, Local data) throws IOException {
        data.render.endTag().endXml();
        write(data.writer, path, writer.toString(), data.config.getString("pmab.xmlEncoding", "UTF-8"));
    }

    private void writeVariants(VariantMap values, String name, String prefix, Local data) throws IOException {
        val render = data.render.beginTag(name);
        for (val pair : values) {
            writeVariant(pair.getFirst(), pair.getSecond(), prefix, data);
        }
        render.endTag();
    }

    private void writeVariant(String name, Object value, String prefix, Local data) throws IOException {
        val render = data.render.beginTag("item").attribute("name", name);
        String type = Variants.getType(value), text;
        if (type == null || type.equals(Variants.STRING)) {
            text = value.toString();
            type = Variants.STRING;
        } else {
            switch (type) {
                case Variants.TEXT: {
                    val obj = (Text) value;
                    text = writeText(obj, prefix + name, data);
                    type = getType(obj, data);
                }
                break;
                case Variants.FLOB: {
                    val obj = (Flob) value;
                    text = writeFlob(obj, prefix + name, data);
                    type = obj.getMime();
                }
                break;
                case Variants.DATETIME: {
                    val format = data.config.getString("pmab.dateFormat", DateUtils.ISO_FORMAT);
                    text = DateUtils.format((Date) value, format);
                    type = type + ";format=" + format;
                }
                break;
                case Variants.LOCALE: {
                    text = MiscUtils.renderLocale((Locale) value);
                    type = Variants.LOCALE;
                }
                break;
                default: {
                    text = value.toString();
                    type = Variants.STRING;
                }
                break;
            }
        }
        render.attribute("type", type).text(text).endTag();
    }

    private String writeFlob(Flob flob, String name, Local data) throws IOException {
        val extName = PathUtils.extName(flob.getName());
        val path = "resources/" + name + (!extName.isEmpty() ? '.' + extName : "");
        write(data.writer, path, flob);
        return path;
    }

    private String writeText(Text text, String name, Local data) throws IOException {
        val path = "text/" + name + '.' + (text.getType().equals(Texts.PLAIN) ? "txt" : text.getType());
        write(data.writer, path, text, data.encoding);
        return path;
    }

    private String getType(Text text, Local data) {
        return "text/" + text.getType() + ";encoding=" + data.encoding;
    }

    private static class Local {
        Book book;
        VdmWriter writer;
        XmlRender render;
        TypedConfig config;
        String encoding;

        @SneakyThrows(XmlPullParserException.class)
        Local(Book book, VdmWriter writer, TypedConfig config) {
            this.book = book;
            this.writer = writer;
            this.config = config;
            this.render = new XmlRender(config);
            encoding = config.getString("pmab.txtEncoding", System.getProperty("file.encoding"));
        }
    }
}
