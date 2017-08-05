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

import jclp.setting.Settings;
import jclp.util.StringUtils;
import jem.Attributes;
import jem.Book;
import jem.epm.Maker;
import jem.epm.impl.VdmMaker;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

import java.io.File;

@Data
@Builder
public class MakerParam {
    @NonNull
    private Book book;

    private File file;

    private String output;

    @NonNull
    private String format;

    private Settings arguments;

    public void initFile(Maker maker, Settings arguments) {
        if (file == null || !file.isDirectory() || !(maker instanceof VdmMaker)) {
            return;
        }
        if (arguments == null || !"dir".equals(arguments.get("maker.vdm.type"))) {
            val title = Attributes.getString(book, Attributes.TITLE);
            if (StringUtils.isNotEmpty(title)) {
                file = new File(file, title + '.' + getFormat());
            }
        }
    }
}
