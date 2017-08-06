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

package jem.util.text;

import jem.util.flob.Flob;
import jem.util.text.impl.FlobText;
import jem.util.text.impl.RawText;

/**
 * Factory for {@code Text}.
 */
public final class Texts {
    private Texts() {
    }

    /**
     * Type for html text.
     */
    public static final String HTML = "html";

    /**
     * Type for plain text.
     */
    public static final String PLAIN = "plain";

    public static RawText empty() {
        return forString("", PLAIN);
    }

    public static RawText forString(CharSequence cs) {
        return forString(cs, PLAIN);
    }

    public static RawText forString(CharSequence cs, String type) {
        return new RawText(type, cs);
    }

    public static FlobText forFlob(Flob flob, String encoding) {
        return forFlob(flob, encoding, PLAIN);
    }

    public static FlobText forFlob(Flob flob, String encoding, String type) {
        return new FlobText(type, flob, encoding);
    }
}
