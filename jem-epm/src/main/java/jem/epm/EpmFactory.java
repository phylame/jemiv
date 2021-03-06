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

package jem.epm;

import jclp.util.NamedService;

/**
 * Factory for creating Epm maker and parser.
 */
public interface EpmFactory extends NamedService {
    /**
     * Returns {@literal true} if maker is supported.
     *
     * @return {@literal true} if maker is supported
     */
    boolean hasMaker();

    /**
     * Returns the {@code Maker} for this format.
     *
     * @return the {@code Maker} instance, or {@literal null} if maker is unsupported
     */
    Maker getMaker();

    /**
     * Returns {@literal true} if parser is supported.
     *
     * @return {@literal true} if parser is supported
     */
    boolean hasParser();

    /**
     * Returns the {@code Parser} for this format.
     *
     * @return the {@code Parser} instance, or {@literal null} if parser is unsupported
     */
    Parser getParser();
}
