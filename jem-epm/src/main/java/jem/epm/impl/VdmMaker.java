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

import jclp.vdm.VdmWriter;
import jem.epm.util.M;
import jem.epm.util.VdmUtils;
import jem.util.TypedConfig;
import lombok.val;

import java.io.File;
import java.io.IOException;

public abstract class VdmMaker extends AbstractMaker<VdmWriter> {
    @Override
    protected VdmWriter open(File file, TypedConfig config) throws IOException {
        val type = config.getString("vdm.type", "zip");
        val writer = VdmUtils.openWriter(file, type);
        if (writer == null) {
            throw new IOException(M.translator().tr("err.vdm.unsupported", type));
        }
        return writer;
    }
}
