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

package jem.epm.impl;

import jclp.io.IOUtils;
import jclp.setting.Settings;
import jem.Book;
import jem.util.TypedConfig;
import jem.epm.util.MakerException;
import jem.epm.util.M;
import jem.util.JemException;
import lombok.val;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static jclp.util.Validate.checkNotNull;

public abstract class AbstractMaker<O extends Closeable> implements FileMaker {
    protected abstract O open(File file, TypedConfig config) throws IOException;

    protected abstract void make(Book book, O output, TypedConfig config) throws IOException, JemException;

    @Override
    public void make(Book book, String output, Settings arguments) throws IOException, JemException {
        make(book, new File(output), arguments);
    }

    @Override
    public void make(Book book, File file, Settings arguments) throws IOException, JemException {
        val config = new TypedConfig("maker.", arguments);
        val output = open(file, config);
        checkNotNull(output, "open(...) of %s returned null", this);
        try {
            make(book, output, config);
        } catch (Exception e) {
            IOUtils.closeQuietly(output);
            throw e;
        }
        output.close();
    }

    protected final MakerException error(String key, Object... args) {
        return new MakerException(M.translator().tr(key, args));
    }
}
