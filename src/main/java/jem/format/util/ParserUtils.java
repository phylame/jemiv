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

package jem.format.util;

import jclp.util.DateUtils;
import jclp.util.MiscUtils;
import jem.epm.util.ParserException;
import lombok.val;
import org.xmlpull.v1.XmlPullParser;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static jclp.util.StringUtils.isNotEmpty;

public final class ParserUtils {
    private ParserUtils() {
    }

    public static ParserException error(String key, Object... args) {
        return new ParserException(M.translator().tr(key, args));
    }

    public static ParserException error(Exception cause, String key, Object... args) {
        return new ParserException(M.translator().tr(key, args), cause);
    }

    public static Locale parseLocale(String str) {
        return MiscUtils.parseLocale(str);
    }

    public static int parseInteger(String str) throws ParserException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw error(e, "err.parser.invalidNumber", str);
        }
    }

    public static double parseDouble(String str) throws ParserException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw error(e, "err.parser.invalidNumber", str);
        }
    }

    public static Date parseDate(String str, String format) throws ParserException {
        try {
            return isNotEmpty(format)
                    ? DateUtils.parse(str, format)
                    : DateUtils.parse(str, new Date());
        } catch (ParseException e) {
            throw error(e, "err.parser.invalidDate", str);
        }
    }

    public static String requiredAttribute(XmlPullParser xpp, String name) throws ParserException {
        val value = xpp.getAttributeValue(null, name);
        if (value == null) {
            throw error("pmab.parse.requiredAttribute", name);
        }
        return value;
    }
}
