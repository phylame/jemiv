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

package jem.format.util.xml;

import jclp.util.StringUtils;
import jem.util.TypedConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

public class XmlRender {
    private TypedConfig config;

    private XmlSerializer serializer;

    private LinkedList<Tag> tags = new LinkedList<>();

    private int depth;

    private String indent;

    private String separator;

    public XmlRender(TypedConfig config) throws XmlPullParserException {
        this(XmlPullParserFactory.newInstance().newSerializer(), config);
    }

    public XmlRender(@NonNull XmlSerializer serializer, @NonNull TypedConfig config) {
        this.serializer = serializer;
        this.config = config;

        indent = config.getString("xml.indent", "");
        separator = config.getString("xml.separator", System.lineSeparator());
    }

    public XmlRender output(Writer writer) throws IOException {
        serializer.setOutput(writer);
        return this;
    }

    public XmlRender flush() throws IOException {
        serializer.flush();
        return this;
    }

    public XmlRender reset() {
        depth = 0;
        tags.clear();
        return this;
    }

    public XmlRender beginXml() throws IOException {
        val encoding = config.getString("xml.encoding", "UTF-8");
        val standalone = config.getBoolean("xml.standalone", false);
        serializer.startDocument(encoding, standalone);
        return reset();
    }

    public void endXml() throws IOException {
        serializer.endDocument();
        flush();
    }

    public XmlRender docdecl(String root, String id, String url) throws IOException {
        return docdecl(root + " PUBLIC \"" + id + "\" \"" + url + "\"");
    }

    public XmlRender docdecl(String text) throws IOException {
        newLine();
        serializer.docdecl(" " + text);
        return this;
    }

    private void indent(int count) throws IOException {
        if (count <= 0) {
            return;
        }
        serializer.text(StringUtils.duplicated(indent, count));
    }

    private void newLine() throws IOException {
        serializer.text(separator);
    }

    private void newNode() throws IOException {
        newLine();
        indent(depth);
        if (!tags.isEmpty()) {
            tags.getFirst().hasChild = true;
        }
    }

    public XmlRender beginTag(String name) throws IOException {
        return beginTag(null, name);
    }

    public XmlRender beginTag(String namespace, String name) throws IOException {
        newNode();
        ++depth;
        serializer.startTag(namespace, name);
        tags.push(new Tag(namespace, name));
        return this;
    }

    public XmlRender attribute(String name, String value) throws IOException {
        serializer.attribute(null, name, value);
        return this;
    }

    public XmlRender attribute(String namespace, String name, String value) throws IOException {
        serializer.attribute(namespace, name, value);
        return this;
    }

    public XmlRender xmlns(String namespace) throws IOException {
        return attribute("xmlns", namespace);
    }

    public XmlRender comment(String text) throws IOException {
        newNode();
        serializer.comment(text);
        return this;
    }

    public XmlRender text(String text) throws IOException {
        serializer.text(text);
        return this;
    }

    public XmlRender endTag() throws IOException {
        if (tags.isEmpty()) {
            throw new AssertionError("startTag should be called firstly");
        }
        val tag = tags.pop();
        if (tag.hasChild) {
            newLine();
            indent(depth - 1);
        }
        --depth;
        serializer.endTag(tag.namespace, tag.name);
        return this;
    }

    @RequiredArgsConstructor
    private class Tag {
        private final String namespace;
        private final String name;

        // for endTag, if hasChild add line separator and indent
        private boolean hasChild = false;
    }
}
