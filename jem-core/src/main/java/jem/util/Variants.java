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

package jem.util;

import jclp.function.Function;
import jclp.function.Provider;
import jclp.io.IOUtils;
import jclp.log.Log;
import jclp.text.ConverterManager;
import jclp.text.Converters;
import jclp.text.Parser;
import jclp.value.Values;
import jem.util.flob.Flob;
import jem.util.flob.Flobs;
import jem.util.text.Text;
import jem.util.text.Texts;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static jclp.util.CollectionUtils.getOrPut;
import static jclp.util.CollectionUtils.propertiesFor;
import static jclp.util.StringUtils.isNotEmpty;
import static jclp.util.Validate.requireNotEmpty;
import static jclp.util.Validate.requireNotNull;

/**
 * Utilities for values used in Jem.
 */
public final class Variants {
    private Variants() {
    }

    private static final String TAG = "Variants";

    // standard type names
    public static final String REAL = "real";
    public static final String INTEGER = "int";
    public static final String BOOLEAN = "bool";
    public static final String STRING = "str";
    public static final String FLOB = "file";
    public static final String TEXT = "text";
    public static final String LOCALE = "locale";
    public static final String DATETIME = "datetime";

    private static Map<String, Class<?>> typeMappings = new HashMap<>();

    private static Map<Class<?>, String> classMappings = new IdentityHashMap<>();

    private static Map<String, Object> typeDefaults = new HashMap<>();

    /**
     * Returns a set containing all type names.
     *
     * @return set of type names
     */
    public static Set<String> getTypes() {
        return typeMappings.keySet();
    }

    /**
     * Associates the specified class with the specified type name.
     *
     * @param type  the type name to mapping
     * @param clazz the class to be mapped
     * @throws NullPointerException if the type name or class is null
     */
    public static void mapClass(String type, @NonNull Class<?> clazz) {
        requireNotEmpty(type, "type name cannot be null or empty");
        typeMappings.put(type, clazz);
        classMappings.put(clazz, type);
    }

    /**
     * Gets the class of specified type name.
     *
     * @param type the type name
     * @return class of the type, or {@literal null} if no class found
     */
    public static Class<?> getClass(String type) {
        return isNotEmpty(type) ? typeMappings.get(type) : null;
    }

    /**
     * Gets the type name of specified value.
     *
     * @param value the value
     * @return the type name of specified value, or {@literal null} if type of the value is unknown
     * @throws NullPointerException if the value is null
     */
    public static String getType(@NonNull Object value) {
        return getOrPut(classMappings, value.getClass(), false, new Function<Class<?>, String>() {
            @Override
            public String apply(Class<?> clazz) {
                for (val entry : classMappings.entrySet()) {
                    if (entry.getKey().isAssignableFrom(clazz)) {
                        return entry.getValue();
                    }
                }
                return null;
            }
        });
    }

    /**
     * Associates the specified default value with the specified type name.
     * <p>The value can be instance of {@link jclp.value.Value}, which {@code get()} method will be invoked
     * when querying defaults.</p>
     *
     * @param type  the type name to mapping
     * @param value the default value to be mapped
     * @throws NullPointerException if the type is null
     */
    public static void setDefault(String type, Object value) {
        requireNotEmpty(type, "type name cannot be null or empty");
        typeDefaults.put(type, value);
    }

    /**
     * Gets default value for specified type name.
     *
     * @param type the type name
     * @return the default value, or {@literal null} if the type is unknown or no default value found
     */
    public static Object getDefault(String type) {
        return isNotEmpty(type) ? Values.get(typeDefaults.get(type)) : null;
    }

    /**
     * Gets a readable text for specified type name.
     *
     * @param type the type name
     * @return a readable text, or {@literal null} if not text found
     * @throws NullPointerException if the type is null
     */
    public static String getName(@NonNull String type) {
        return M.translator().optTr("variant." + type, null);
    }

    /**
     * Makes a printable text for specified value.
     *
     * @param value the value to be made
     * @return a printable text for specified value, or {@literal null} if class of value is unknown
     * @throws NullPointerException if the value is null
     */
    public static String printable(@NonNull Object value) {
        val type = getType(value);
        if (type == null) {
            return null;
        }
        switch (type) {
            case STRING:
            case BOOLEAN:
            case INTEGER:
            case REAL:
            case TEXT:
            case FLOB:
                return value.toString();
            case DATETIME:
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format((Date) value);
            case LOCALE:
                return ((Locale) value).getDisplayName();
            default:
                return null;
        }
    }

    /**
     * Parses specified string value to object with specified type.
     * <p>This method is backed by JCLP Converter API</p>
     *
     * @param value the string value to be parsed
     * @param type  the type name of the value
     * @return the object value, or {@literal null} if the type is unknown
     * @throws NullPointerException if the value or type is null
     * @see jclp.text.Converters
     * @see jclp.text.ConverterManager
     */
    public static Object parse(@NonNull String value, @NonNull String type) {
        Class<?> clazz = getClass(type);
        if (clazz == null || CharSequence.class.isAssignableFrom(clazz)) {
            return value;
        } else if (Text.class.isAssignableFrom(clazz)) {
            clazz = Text.class;
        } else if (Flob.class.isAssignableFrom(clazz)) {
            clazz = Flob.class;
        }
        return Converters.parse(value, clazz);
    }

    private static void initBuiltins() {
        Properties map = null;
        try {
            map = propertiesFor("!jem/util/variants.properties");
        } catch (IOException e) {
            Log.e(TAG, "cannot load variant type mapping", e);
        }
        if (map == null) {
            return;
        }
        for (val entry : map.entrySet()) {
            try {
                mapClass(entry.getValue().toString().trim(), Class.forName(entry.getKey().toString()));
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "not found variant class", e);
            }
        }
    }

    private static void initDefaults() {
        setDefault(REAL, 0.0D);
        setDefault(INTEGER, 0);
        setDefault(STRING, "");
        setDefault(BOOLEAN, false);
        setDefault(DATETIME, Values.lazy(new Provider<Date>() {
            @Override
            public Date provide() throws Exception {
                return new Date();
            }
        }));
        setDefault(LOCALE, Values.lazy(new Provider<Locale>() {
            @Override
            public Locale provide() throws Exception {
                return Locale.getDefault();
            }
        }));
        setDefault(FLOB, Flobs.empty());
        setDefault(TEXT, Texts.empty());
    }

    private static void registerParsers() {
        ConverterManager.registerParser(Text.class, new Parser<Text>() {
            @Override
            public Text parse(String str) {
                return Texts.forString(str, Texts.PLAIN);
            }
        });
        ConverterManager.registerParser(Flob.class, new Parser<Flob>() {
            @Override
            public Flob parse(String str) {
                URL url;
                try {
                    url = IOUtils.resourceFor(str);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("bad url " + str, e);
                }
                requireNotNull(url, "no such resource %s", str);
                return Flobs.forURL(url);
            }
        });
    }

    static {
        initBuiltins();
        initDefaults();
        registerParsers();
    }
}
