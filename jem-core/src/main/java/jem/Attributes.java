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

package jem;

import jclp.log.Log;
import jem.util.M;
import jem.util.VariantMap;
import jem.util.VariantMap.Validator;
import jem.util.Variants;
import jem.util.flob.Flob;
import jem.util.text.Text;
import jem.util.text.Texts;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.*;

import static jclp.util.CollectionUtils.propertiesFor;
import static jclp.util.CollectionUtils.update;
import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.StringUtils.join;
import static jclp.util.Validate.require;

/**
 * Constants and utilities for chapter attributes.
 */
public final class Attributes {
    private Attributes() {
    }

    private static final String TAG = "Attributes";

    // standard attributes
    public static final String AUTHOR = "author";
    public static final String COVER = "cover";
    public static final String DATE = "date";
    public static final String GENRE = "genre";
    public static final String INTRO = "intro";
    public static final String ISBN = "isbn";
    public static final String KEYWORDS = "keywords";
    public static final String LANGUAGE = "language";
    public static final String PRICE = "price";
    public static final String PUBDATE = "pubdate";
    public static final String PUBLISHER = "publisher";
    public static final String RIGHTS = "rights";
    public static final String SERIES = "series";
    public static final String STATE = "state";
    public static final String TITLE = "title";
    public static final String VENDOR = "vendor";
    public static final String WORDS = "words";

    public static final String VALUES_SEPARATOR = ";";

    private static Map<String, String> typeMappings = new HashMap<>();

    private static final Validator typeValidator = new Validator() {
        @Override
        public void validate(String name, Object value) throws IllegalArgumentException {
            val type = getType(name);
            if (type == null) {
                return;
            }
            val clazz = Variants.getClass(type);
            if (clazz != null) {
                require(clazz.isAssignableFrom(value.getClass()), "attribute '%s' must be '%s'", name, clazz.getName());
            }
        }
    };

    public static VariantMap newAttributes() {
        return new VariantMap(typeValidator);
    }

    /**
     * Returns a set containing known attribute names.
     *
     * @return set of attribute names
     */
    public static Set<String> getNames() {
        return typeMappings.keySet();
    }

    /**
     * Associates the specified type with the specified attribute name.
     *
     * @param name the attribute name to mapping
     * @param type the value type to be mapped
     * @throws NullPointerException if the attribute name or type name is null
     */
    public static void mapType(@NonNull String name, @NonNull String type) {
        typeMappings.put(name, type);
    }

    /**
     * Returns the type name for specified attribute name.
     *
     * @param name the attribute name
     * @return the type name, or {@literal null} if attribute name is unknown
     * @throws NullPointerException if specified attribute name is null
     */
    public static String getType(@NonNull String name) {
        return typeMappings.get(name);
    }

    /**
     * Gets a readable text for specified attribute name.
     *
     * @param name the attribute name
     * @return a readable text, or {@literal null} if not text found
     * @throws NullPointerException if specified attribute name is null
     */
    public static String getName(@NonNull String name) {
        return M.translator().optTr("attribute." + name, null);
    }

    /**
     * Gets attribute value for specified attribute name of specified chapter.
     *
     * @param chapter the chapter to be retrieved attributes
     * @param name    the attribute name
     * @return the attribute value, or {@literal null} if no attribute found
     * @throws NullPointerException if the chapter or name is null
     */
    public static <T> T getValue(@NonNull Chapter chapter, String name) {
        return chapter.getAttributes().get(name, null);
    }

    /**
     * Gets string value for specified attribute of specified chapter.
     *
     * @param chapter the chapter to be retrieved attributes
     * @param name    the attribute name
     * @return the string value, or {@literal ""} if no attribute found
     * @throws NullPointerException if the chapter or name is null
     * @throws ClassCastException   if the attribute value is not a string
     */
    public static String getString(@NonNull Chapter chapter, String name) {
        return chapter.getAttributes().get(name, "");
    }

    /**
     * Gets string values for specified attribute of specified chapter.
     *
     * @param chapter the chapter to be retrieved attributes
     * @param name    the attribute name
     * @return the string values, or {@literal null} if no attribute found
     * @throws NullPointerException if the chapter or name is null
     * @throws ClassCastException   if the attribute value is not a string
     */
    public static Collection<String> getValues(@NonNull Chapter chapter, String name) {
        val value = getString(chapter, name);
        return isNotEmpty(value) ? Arrays.asList(value.split(VALUES_SEPARATOR)) : null;
    }

    /**
     * Sets specified value for specified attribute name of specified chapter.
     *
     * @param chapter the chapter to be set attributes
     * @param name    the attribute name
     * @param value   the new attribute value
     * @throws NullPointerException if the chapter, name or value is null
     */
    public static void setValue(@NonNull Chapter chapter, String name, Object value) {
        chapter.getAttributes().set(name, value);
    }

    /**
     * Sets specified values for specified attribute name of specified chapter.
     *
     * @param chapter the chapter to be set attributes
     * @param name    the attribute name
     * @param values  the new attribute values
     * @throws NullPointerException if the chapter, name or values is null
     */
    public static void setValues(@NonNull Chapter chapter, String name,
                                 @NonNull Collection<? extends CharSequence> values) {
        setValue(chapter, name, join(VALUES_SEPARATOR, values.iterator()));
    }

    public static String getTitle(Chapter chapter) {
        return getString(chapter, TITLE);
    }

    public static void setTitle(Chapter chapter, String title) {
        setValue(chapter, TITLE, title);
    }

    public static String getAuthor(Chapter chapter) {
        return getString(chapter, AUTHOR);
    }

    public static void setAuthor(Chapter chapter, String author) {
        setValue(chapter, AUTHOR, author);
    }

    public static Flob getCover(Chapter chapter) {
        return getValue(chapter, COVER);
    }

    public static void setCover(Chapter chapter, Flob cover) {
        setValue(chapter, COVER, cover);
    }

    public static Text getIntro(Chapter chapter) {
        return getValue(chapter, INTRO);
    }

    public static void setIntro(Chapter chapter, Text intro) {
        setValue(chapter, INTRO, intro);
    }

    public static void setIntro(Chapter chapter, String intro) {
        setIntro(chapter, Texts.forString(intro));
    }

    public static String getGenre(Chapter chapter) {
        return getString(chapter, GENRE);
    }

    public static void setGenre(Chapter chapter, String genre) {
        setValue(chapter, GENRE, genre);
    }

    public static Date getDate(Chapter chapter) {
        return getValue(chapter, DATE);
    }

    public static void setDate(Chapter chapter, Date date) {
        setValue(chapter, DATE, date);
    }

    public static Date getPubdate(Chapter chapter) {
        return getValue(chapter, PUBDATE);
    }

    public static void setPubdate(Chapter chapter, Date pubdate) {
        setValue(chapter, PUBDATE, pubdate);
    }

    public static String getState(Chapter chapter) {
        return getString(chapter, STATE);
    }

    public static void setState(Chapter chapter, String state) {
        setValue(chapter, STATE, state);
    }

    public static Locale getLanguage(Chapter chapter) {
        return getValue(chapter, LANGUAGE);
    }

    public static void setLanguage(Chapter chapter, Locale language) {
        setValue(chapter, LANGUAGE, language);
    }

    public static String getPublisher(Chapter chapter) {
        return getString(chapter, PUBLISHER);
    }

    public static void setPublisher(Chapter chapter, String publisher) {
        setValue(chapter, PUBLISHER, publisher);
    }

    public static String getRights(Chapter chapter) {
        return getString(chapter, RIGHTS);
    }

    public static void setRights(Chapter chapter, String rights) {
        setValue(chapter, RIGHTS, rights);
    }

    public static String getVendor(Chapter chapter) {
        return getString(chapter, VENDOR);
    }

    public static void setVendor(Chapter chapter, String vendor) {
        setValue(chapter, VENDOR, vendor);
    }

    public static String getWords(Chapter chapter) {
        return getString(chapter, WORDS);
    }

    public static void setWords(Chapter chapter, String words) {
        setValue(chapter, WORDS, words);
    }

    public static void setWords(Chapter chapter, int words) {
        setWords(chapter, Integer.toString(words));
    }

    private static void initBuiltins() {
        Properties map;
        try {
            map = propertiesFor("!jem/attributes.properties");
        } catch (IOException e) {
            Log.e(TAG, "cannot load attribute type mapping", e);
            return;
        }
        if (map == null) {
            return;
        }
        update(typeMappings, map);
    }

    static {
        initBuiltins();
    }
}
