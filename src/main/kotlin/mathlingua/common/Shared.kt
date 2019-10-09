/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mathlingua.common

import kotlin.collections.ArrayList

data class ParseError(
    override val message: String,
    val row: Int,
    val column: Int
) : RuntimeException(message)

sealed class Validation<out T>
data class ValidationSuccess<T>(val value: T) : Validation<T>()
data class ValidationFailure<T>(val errors: List<ParseError>) : Validation<T>()

class Stack<T> {
    private val data = mutableListOf<T>()

    fun push(item: T) = data.add(item)

    fun pop() = data.removeAt(data.size - 1)

    fun peek() = data.elementAt(data.size - 1)

    fun isEmpty() = data.isEmpty()
}

class Queue<T> : Iterable<T> {
    private val data = ArrayList<T>()

    fun offer(item: T) = data.add(0, item)

    fun poll() = data.removeAt(0)

    fun peek() = data.elementAt(0)

    fun isEmpty() = data.isEmpty()

    override fun iterator() = data.iterator()
}

private fun tokenize(text: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < text.length) {
        if (text[i] == ' ') {
            val buffer = StringBuilder()
            while (i < text.length && text[i] == ' ') {
                buffer.append(text[i++])
            }
            tokens.add(buffer.toString())
        } else {
            val buffer = StringBuilder()
            while (i < text.length && text[i] != ' ') {
                buffer.append(text[i++])
            }
            tokens.add(buffer.toString())
        }
    }
    return tokens
}

fun justify(text: String, width: Int): List<String> {
    val tokens = tokenize(text)
    val lines = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val curLine = StringBuilder()
        while (i < tokens.size && curLine.isEmpty() && tokens[i].isBlank()) {
            i++
        }
        while (i < tokens.size && curLine.length + tokens[i].length <= width) {
            curLine.append(tokens[i++])
        }
        if (i < tokens.size && curLine.isEmpty()) {
            curLine.append(tokens[i++])
        }
        lines.add(curLine.toString())
    }
    return lines
}
