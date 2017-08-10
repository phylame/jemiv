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

package jem.epm.util;

import jclp.io.IOUtils;
import jclp.vdm.VdmManager;
import jclp.vdm.VdmReader;
import jclp.vdm.VdmWriter;
import jclp.vdm.file.FileVdmReader;
import jclp.vdm.zip.ZipVdmReader;
import jem.util.flob.Flob;
import jem.util.text.Text;
import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public final class VdmUtils {
    private VdmUtils() {
    }

    public static VdmWriter openWriter(Object output, String type) throws IOException {
        return VdmManager.getWriter(type, output);
    }

    public static VdmReader openReader(Object input, String type) throws IOException {
        return VdmManager.getReader(type, input);
    }

    public static VdmReader openReader(File file) throws IOException {
        return file.isDirectory() ? new FileVdmReader(file) : new ZipVdmReader(new ZipFile(file));
    }

    public static InputStream getStream(@NonNull VdmReader reader, String name) throws IOException {
        val entry = reader.getEntry(name);
        if (entry == null) {
            throw new IOException(M.translator().tr("err.vdm.noEntry", name, reader.getName()));
        }
        return reader.getInputStream(entry);
    }

    public static String getText(VdmReader reader, String name, String encoding) throws IOException {
        try (val in = getStream(reader, name)) {
            return IOUtils.toString(in, encoding);
        }
    }

    public static void write(VdmWriter writer, String name, String str, String encoding) throws IOException {
        val entry = writer.newEntry(name);
        writer.putEntry(entry)
                .write(encoding == null ? str.getBytes() : str.getBytes(encoding));
        writer.closeEntry(entry);
    }

    public static void write(VdmWriter writer, String name, Flob flob) throws IOException {
        val entry = writer.newEntry(name);
        flob.writeTo(writer.putEntry(entry));
        writer.closeEntry(entry);
    }

    public static void write(VdmWriter writer, String name, Text text, String encoding) throws IOException {
        val entry = writer.newEntry(name);
        val output = IOUtils.writerFor(writer.putEntry(entry), encoding);
        text.writeTo(output);
        output.flush();
        writer.closeEntry(entry);
    }
}
