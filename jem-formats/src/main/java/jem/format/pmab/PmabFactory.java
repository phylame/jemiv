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

import jclp.function.Provider;
import jclp.util.CollectionUtils;
import jclp.value.Lazy;
import jem.epm.Maker;
import jem.epm.Parser;
import jem.epm.impl.AbstractFactory;

import java.util.Set;

public class PmabFactory extends AbstractFactory {
    private final Lazy<Parser> parser = new Lazy<>(new Provider<Parser>() {
        @Override
        public Parser provide() throws Exception {
            return new PmabParser();
        }
    });

    private final Lazy<Maker> maker = new Lazy<>(new Provider<Maker>() {
        @Override
        public Maker provide() throws Exception {
            return new PmabMaker();
        }
    });

    @Override
    public String getName() {
        return "PMAB for Jem";
    }

    @Override
    public Set<String> getNames() {
        return CollectionUtils.setOf("pmab");
    }

    @Override
    public boolean hasParser() {
        return true;
    }

    @Override
    public Parser getParser() {
        return parser.get();
    }

    @Override
    public boolean hasMaker() {
        return true;
    }

    @Override
    public Maker getMaker() {
        return maker.get();
    }
}
