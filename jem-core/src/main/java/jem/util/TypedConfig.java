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

import jclp.setting.PropertiesSettings;
import jclp.setting.Settings;
import jclp.value.Values;

public class TypedConfig {
    private String prefix;

    private Settings settings;

    public TypedConfig(Settings settings) {
        this("", settings);
    }

    public TypedConfig(String prefix, Settings settings) {
        this.prefix = prefix;
        this.settings = settings;
    }

    public int getInt(String key, int fallback) {
        return get(key, Integer.class, fallback);
    }

    public String getString(String key, String fallback) {
        return get(key, String.class, fallback);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return get(key, Boolean.class, fallback);
    }

    public <T> T get(String key, Class<T> type, T fallback) {
        if (settings == null) {
            return fallback;
        }
        T value;
        try {
            value = settings.get(prefix + key, type);
        } catch (Exception e) {
            return fallback;
        }
        return Values.get(value != null ? value : fallback);
    }

    public void set(String key, Object value) {
        if (settings == null) {
            settings = new PropertiesSettings();
        }
        settings.set(key, value);
    }

    public void remove(String key) {
        if (settings != null) {
            settings.remove(key);
        }
    }

    @Override
    public String toString() {
        return "TypedConfig{prefix='" + prefix + '\'' + ", settings=" + settings + '}';
    }
}
