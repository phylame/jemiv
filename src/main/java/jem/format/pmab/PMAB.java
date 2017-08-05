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

package jem.format.pmab;

/**
 * Constants for PMAB.
 */
public interface PMAB {
    /////** MIME type for PMAB **\\\\\
    String MIME_PATH = "mimetype";
    String MIME_PMAB = "application/pmab+zip";

    /////** PBM(PMAB Book Metadata) **\\\\\
    String PBM_PATH = "book.xml";
    String PBM_XMLNS = "http://phylame.pw/format/pmab/pbm";

    /////** PBC(PMAB Book Contents) **\\\\\
    String PBC_PATH = "content.xml";
    String PBC_XML_NS = "http://phylame.pw/format/pmab/pbc";
}
