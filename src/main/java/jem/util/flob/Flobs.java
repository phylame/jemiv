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

package jem.util.flob;

import jclp.io.PathUtils;
import jclp.vdm.VdmReader;
import jem.util.flob.impl.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * Factory class for {@code Flob}.
 */
public final class Flobs {
    private Flobs() {
    }

    public static ByteFlob empty() {
        return forBytes("_empty_", new byte[0], PathUtils.UNKNOWN_MIME);
    }

    public static FileFlob forFile(File file) {
        return forFile(file, null);
    }

    public static FileFlob forFile(File file, String mime) {
        return new FileFlob(file, mime);
    }

    public static VdmFlob forVdm(VdmReader reader, String name) {
        return forVdm(reader, name, null);
    }

    public static VdmFlob forVdm(VdmReader reader, String name, String mime) {
        return new VdmFlob(reader, name, mime);
    }

    public static BlockFlob forBlock(String name, RandomAccessFile file, long offset, long size) {
        return forBlock(name, file, offset, size, null);
    }

    public static BlockFlob forBlock(String name, RandomAccessFile file, long offset, long size, String mime) {
        return new BlockFlob(name, file, offset, size, mime);
    }

    public static URLFlob forURL(URL url) {
        return forURL(url, null);
    }

    public static URLFlob forURL(URL url, String mime) {
        return new URLFlob(url, mime);
    }

    public static ByteFlob forBytes(String name, byte[] bytes) {
        return forBytes(name, bytes, null);
    }

    public static ByteFlob forBytes(String name, byte[] bytes, String mime) {
        return new ByteFlob(name, bytes, mime);
    }
}
