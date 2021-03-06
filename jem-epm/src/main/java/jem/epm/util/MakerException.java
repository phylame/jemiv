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

import jem.util.JemException;

/**
 * Exception for {@code Maker} errors.
 */
@SuppressWarnings("serial")
public class MakerException extends JemException {
    public MakerException() {
    }

    public MakerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MakerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MakerException(String message) {
        super(message);
    }

    public MakerException(Throwable cause) {
        super(cause);
    }
}
