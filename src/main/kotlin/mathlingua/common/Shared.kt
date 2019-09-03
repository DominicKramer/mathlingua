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

data class ParseError(override val message: String, val row: Int, val column: Int) : RuntimeException(message)

class Validation<T> private constructor(val value: T?, val errors: List<ParseError>) {

    val isSuccessful: Boolean
        get() = this.value != null

    companion object {

        fun <T> success(value: T): Validation<T> {
            if (value == null) {
                throw IllegalArgumentException("A successful validation cannot have a null value")
            }
            return Validation(value, ArrayList())
        }

        fun <T> failure(errors: List<ParseError>): Validation<T> {
            return Validation(null, errors)
        }
    }
}

class Stack<T> {
    private val data = mutableListOf<T>()

    fun push(item: T) {
        data.add(item)
    }

    fun pop(): T {
        return data.removeAt(data.size - 1)
    }

    fun peek(): T {
        return data.elementAt(data.size - 1)
    }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }
}

class Queue<T> : Iterable<T> {
    private val data = ArrayList<T>()

    fun offer(item: T) {
        data.add(0, item)
    }

    fun poll(): T {
        return data.removeAt(0)
    }

    fun peek(): T {
        return data.elementAt(0)
    }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return data.iterator()
    }
}
