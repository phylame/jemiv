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

package jem.kotlin

import jem.Attributes.*
import jem.Chapter
import jem.util.flob.Flob
import jem.util.text.Text
import java.util.*

var Chapter.title: String
    get() = getTitle(this)
    set(value) {
        setTitle(this, value)
    }

var Chapter.cover: Flob?
    get() = getCover(this)
    set(value) {
        setCover(this, value)
    }

var Chapter.intro: Text?
    get() = getIntro(this)
    set(value) {
        setIntro(this, value)
    }

var Chapter.words: String
    get() = getWords(this)
    set(value) {
        setWords(this, value)
    }

var Chapter.author: String
    get() = getAuthor(this)
    set(value) {
        setAuthor(this, value)
    }

var Chapter.date: Date?
    get() = getDate(this)
    set(value) {
        setDate(this, value)
    }

var Chapter.pubdate: Date?
    get() = getPubdate(this)
    set(value) {
        setPubdate(this, value)
    }

var Chapter.genre: String
    get() = getGenre(this)
    set(value) {
        setGenre(this, value)
    }

var Chapter.language: Locale?
    get() = getLanguage(this)
    set(value) {
        setLanguage(this, value)
    }

var Chapter.publisher: String
    get() = getPublisher(this)
    set(value) {
        setPublisher(this, value)
    }

var Chapter.rights: String
    get() = getRights(this)
    set(value) {
        setRights(this, value)
    }

var Chapter.state: String
    get() = getState(this)
    set(value) {
        setState(this, value)
    }

var Chapter.vendor: String
    get() = getVendor(this)
    set(value) {
        setVendor(this, value)
    }

operator inline fun <reified T : Any> Chapter.get(name: String): T? = attributes.get(name, null)

operator fun Chapter.set(name: String, value: Any): Any? = attributes.set(name, value)
