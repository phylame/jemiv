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

package jem.util.flob.impl;

import jclp.vdm.VdmEntry;
import jclp.vdm.VdmReader;
import jem.util.flob.AbstractFlob;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;

import static jclp.util.Validate.requireNotNull;

public class VdmFlob extends AbstractFlob {
    private final VdmReader reader;
    private final VdmEntry entry;

    public VdmFlob(@NonNull VdmReader reader, String name, String mime) {
        super(mime);
        entry = reader.entryFor(name);
        requireNotNull(entry, "no such entry %s", name);
        this.reader = reader;
    }

    @Override
    public String getName() {
        return entry.getName();
    }

    @Override
    public InputStream openStream() throws IOException {
        return reader.streamFor(entry);
    }

    @Override
    public String toString() {
        return entry.toString() + ";mime=" + getMime();
    }
}
