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

import jclp.function.Function;
import jclp.function.Predicate;
import jclp.text.Render;
import jclp.util.RandomUtils;
import jclp.util.Sequence;
import lombok.NonNull;
import lombok.val;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.StringUtils.trimmed;

public final class SoupUtils {
    private SoupUtils() {
    }

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; InfoPath.2; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; 360SE)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; TencentTraveler 4.0; .NET CLR 2.0.50727)",
            "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; The World)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon 2.0)",
            "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"
    };

    public static String randomAgent() {
        return RandomUtils.anyOf(USER_AGENTS);
    }

    public static String queryText(Element base, String query) {
        return queryText(base, query, "");
    }

    public static String queryText(Elements base, String query) {
        return queryText(base, query, "");
    }

    public static String queryText(Element base, String query, String separator) {
        return joinText(base.select(query), separator);
    }

    public static String queryText(Elements base, String query, String separator) {
        return joinText(base.select(query), separator);
    }

    public static String queryLink(Element base, String query) {
        val element = base.select(query).first();
        return element != null ? element.absUrl("src") : null;
    }

    public static String queryLink(Elements base, String query) {
        val element = base.select(query).first();
        return element != null ? element.absUrl("src") : null;
    }

    public static String firstText(@NonNull Element element) {
        return getText(element, 0);
    }

    public static String getText(@NonNull Element element, int index) {
        int i = 0;
        for (val node : element.childNodes()) {
            if (node instanceof TextNode) {
                val text = unquote(((TextNode) node).text());
                if (isNotEmpty(text) && index == i++) {
                    return text;
                }
            }
        }
        return "";
    }

    public static String joinText(@NonNull Elements elements, final String separator) {
        return new Sequence<>(elements.iterator())
                .map(new Function<Node, String>() {
                    @Override
                    public String apply(Node node) {
                        return joinText(node, separator);
                    }
                }).join(separator);
    }

    public static String joinText(@NonNull Node node, String separator) {
        return new Sequence<>(node.childNodes().iterator())
                .map(nodeToString)
                .filter(stringNotEmpty)
                .join(separator);
    }

    public static String unquote(String str) {
        return trimmed(str.replace("\u00A0", ""));
    }

    private static final NodeToString nodeToString = new NodeToString();

    public static final StringNotEmpty stringNotEmpty = new StringNotEmpty();

    private static class NodeToString implements Render<Node>, Function<Node, String> {
        @Override
        public String render(Node node) {
            if (node instanceof TextNode) {
                return unquote(((TextNode) node).text());
            } else if (node instanceof Element) {
                return unquote(((Element) node).text());
            }
            return "";
        }

        @Override
        public String apply(Node node) {
            return render(node);
        }
    }

    private static class StringNotEmpty implements Predicate<String> {
        @Override
        public boolean test(String arg) {
            return isNotEmpty(arg);
        }
    }
}
