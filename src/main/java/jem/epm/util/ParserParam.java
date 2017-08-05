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

import jclp.io.PathUtils;
import jclp.setting.Settings;
import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class ParserParam {
    private File file;

    private String input;

    private String format;

    private Settings arguments;

    public String getFormat() {
        if (format != null) {
            return format;
        } else if (file != null) {
            return PathUtils.extName(file.getName());
        } else if (input != null) {
            return PathUtils.extName(input);
        }
        throw new IllegalStateException("no format or input specified");
    }
}
