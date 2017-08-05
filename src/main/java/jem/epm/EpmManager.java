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

import jclp.util.ServiceManager;
import jem.Book;
import jem.epm.impl.FileMaker;
import jem.epm.impl.FileParser;
import jem.epm.util.MakerParam;
import jem.epm.util.ParserParam;
import jem.util.JemException;
import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.io.IOException;

/**
 * Manager class for Epm providers.
 */
public class EpmManager extends ServiceManager<EpmFactory> {
    public EpmManager() {
        super(EpmFactory.class);
    }

    public EpmManager(ClassLoader loader) {
        super(EpmFactory.class, loader);
    }

    /**
     * Gets parser for specified epm name.
     *
     * @param name the epm name
     * @return a {@code Parser} instance, or {@literal null} if specified name is not found or unsupported
     */
    public Parser getParser(@NonNull String name) {
        val factory = getService(name);
        return factory != null ? factory.getParser() : null;
    }

    /**
     * Gets maker for specified epm name.
     *
     * @param name the epm name
     * @return a {@code Maker} instance, or {@literal null} if specified name is not found or unsupported
     */
    public Maker getMaker(@NonNull String name) {
        val factory = getService(name);
        return factory != null ? factory.getMaker() : null;
    }

    /**
     * Reads book from specified input parameters.
     *
     * @param param the parameters to epm {@code Parser}
     * @return a {@code Book} for specified input, or {@literal null} if the epm name is unsupported
     * @throws IOException  if error occurs when parsing book
     * @throws JemException if I/O error occurs
     */
    public Book readBook(@NonNull ParserParam param) throws IOException, JemException {
        val parser = getParser(param.getFormat());
        if (parser == null) {
            return null;
        }
        val file = param.getFile();
        return file != null && parser instanceof FileParser
                ? ((FileParser) parser).parse(file, param.getArguments())
                : parser.parse(param.getInput(), param.getArguments());
    }

    /**
     * Writes book to output in specified parameters.
     *
     * @param param the parameters to epm {@code Maker}
     * @return {@literal true} if success, or {@literal false} if the epm name is unsupported
     * @throws IOException  if error occurs when making book
     * @throws JemException if I/O error occurs
     */
    public boolean writeBook(@NonNull MakerParam param) throws IOException, JemException {
        val maker = getMaker(param.getFormat());
        if (maker == null) {
            return false;
        }
        File file = param.getFile();
        if (file != null && maker instanceof FileMaker) {
            param.initFile(maker, param.getArguments());
            ((FileMaker) maker).make(param.getBook(), param.getFile(), param.getArguments());
        } else {
            maker.make(param.getBook(), param.getOutput(), param.getArguments());
        }
        return true;
    }
}
