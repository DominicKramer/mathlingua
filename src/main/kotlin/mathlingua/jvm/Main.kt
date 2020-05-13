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

package mathlingua.jvm

import mathlingua.common.MathLingua
import mathlingua.common.ParseError
import mathlingua.common.ValidationFailure
import mathlingua.common.ValidationSuccess
import java.io.File
import kotlin.system.exitProcess

private fun bold(text: String) = "\u001B[1m$text\u001B[0m"
private fun green(text: String) = "\u001B[32m$text\u001B[0m"
private fun red(text: String) = "\u001B[31m$text\u001B[0m"
private fun yellow(text: String) = "\u001B[33m$text\u001B[0m"

private enum class OutputType {
    Json,
    TestCase,
    UserFocused
}

private data class ErrorInfo(
    val file: File,
    val message: String,
    val failedLine: String,
    val row: Int,
    val column: Int
) {
    fun toString(type: OutputType): String {
        val builder = StringBuilder()
        when (type) {
            OutputType.Json -> {
                println("{")
                println("  \"file\": \"${file.normalize().absolutePath}\",")
                println("  \"message\": \"${message.replace("\n", "\\n")}\",")
                println("  \"failedLine\": \"${failedLine.replace("\n", "\\n")}\",")
                println("  \"row\": $row,")
                println("  \"column\": $column")
                println("}")
            }
            OutputType.TestCase -> {
                println("Row: $row")
                println("Column: $column")
                println("Message:")
                println(message)
                println("EndMessage:")
            }
            else -> {
                println(bold("File: $file"))
                println(failedLine.trim())
                println(message.trim())
            }
        }
        return builder.toString()
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty() || args.contains("--help")) {
        println("Usage: mlg [--json] <mathlingua file...>")
        exitProcess(1)
    }

    val files = mutableListOf<File>()
    var printJson = false
    var failOnWarnings = false
    var generateTestCases = false
    for (arg in args) {
        if (arg == "--json") {
            printJson = true
        } else if (arg == "--failOnWarnings") {
            failOnWarnings = true
        } else if (arg == "--generateTestCases") {
            generateTestCases = true
        } else if (arg.startsWith("--")) {
            println("Unrecognized argument $arg")
            exitProcess(1)
        } else {
            files.addAll(findFiles(File(arg), ".math"))
        }
    }

    val allSignatures = mutableSetOf<String>()
    val defSignatures = mutableSetOf<String>()

    val allErrorInfo = mutableListOf<ErrorInfo>()
    for (f in files) {
        allErrorInfo.addAll(processFile(f, allSignatures, defSignatures))
    }

    if (printJson && generateTestCases) {
        println("Cannot specify both --failOnWarnings and --generateTestCases")
        exitProcess(1)
    }

    when {
        printJson -> {
            println("[")
            for (i in 0 until allErrorInfo.size) {
                print(allErrorInfo[i].toString(OutputType.Json))
                if (i != allErrorInfo.size - 1) {
                    println(",")
                }
            }
            println("]")
        }
        generateTestCases -> {
            for (err in allErrorInfo) {
                println(err.toString(OutputType.TestCase))
                println()
            }
        }
        else -> {
            for (err in allErrorInfo) {
                println(err.toString(OutputType.UserFocused))
            }
        }
    }

    val prefix = if (failOnWarnings) {
        "ERROR:"
    } else {
        "WARNING:"
    }

    val notDefinedSignatures = allSignatures.minus(defSignatures)
    if (notDefinedSignatures.isNotEmpty()) {
        val signatureOrSignatures = if (notDefinedSignatures.size == 1) {
            "signature is"
        } else {
            "signatures are"
        }
        val coloredPrefix = if (failOnWarnings) {
            bold(red(prefix))
        } else {
            bold(yellow(prefix))
        }
        println("$coloredPrefix The following ${notDefinedSignatures.size} " +
            "$signatureOrSignatures used but not defined:")
    }

    val indent = " ".repeat(prefix.length + 1)
    for (sig in notDefinedSignatures) {
        println("$indent${bold(sig)}")
    }

    val failed = allErrorInfo.isNotEmpty() || (failOnWarnings && notDefinedSignatures.isNotEmpty())
    if (!printJson && !generateTestCases) {
        val fileOrFiles = if (files.size > 1) {
            "files"
        } else {
            "file"
        }
        if (failed) {
            if (failOnWarnings) {
                println("${bold(red("FAILURE:"))} Found ${allErrorInfo.size + notDefinedSignatures.size} errors from ${files.size} $fileOrFiles")
            } else {
                println("${bold(red("FAILURE:"))} Found ${allErrorInfo.size} errors and ${notDefinedSignatures.size} warnings in ${files.size} $fileOrFiles")
            }
        } else {
            println("${bold(green("SUCCESS:"))} Processed ${files.size} $fileOrFiles with ${notDefinedSignatures.size} warnings")
        }
    }

    exitProcess(if (failed) 1 else 0)
}

private fun getErrorInfo(err: ParseError, file: File, inputLines: List<String>): ErrorInfo {
    val lineNumber = "Line ${err.row + 1}: "
    val lineBuilder = StringBuilder()
    lineBuilder.append(lineNumber)
    if (err.row >= 0 && err.row < inputLines.size) {
        lineBuilder.append(inputLines[err.row])
        lineBuilder.append('\n')
        for (i in lineNumber.indices) {
            lineBuilder.append(' ')
        }
        for (i in 0 until err.column) {
            lineBuilder.append(' ')
        }
        lineBuilder.append("^\n")
    }

    return ErrorInfo(
        file,
        err.message,
        lineBuilder.toString(),
        err.row, err.column
    )
}

private fun processFile(file: File, allSignatures: MutableSet<String>, defSignatures: MutableSet<String>): List<ErrorInfo> {
    val input = String(file.readBytes())
    val inputLines = input.split("\n")

    return when (val validation = MathLingua.parse(input)) {
        is ValidationSuccess -> {
            val document = validation.value
            allSignatures.addAll(MathLingua.findAllSignatures(document))

            for (def in document.defines) {
                if (def.signature != null) {
                    defSignatures.add(def.signature)
                }
            }

            for (rep in document.represents) {
                if (rep.signature != null) {
                    defSignatures.add(rep.signature)
                }
            }
            emptyList()
        }
        is ValidationFailure -> validation.errors.map {
            getErrorInfo(it, file, inputLines)
        }
    }
}

private fun findFiles(file: File, ext: String): List<File> {
    if (file.isFile) {
        return if (file.absolutePath.endsWith(ext)) {
            listOf(file)
        } else {
            emptyList()
        }
    }

    val result = mutableListOf<File>()
    val children = file.listFiles() ?: arrayOf()
    for (child in children) {
        result.addAll(findFiles(child, ext))
    }

    return result
}
