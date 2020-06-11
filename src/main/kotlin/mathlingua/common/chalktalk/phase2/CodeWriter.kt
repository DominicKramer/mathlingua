/*
 * Copyright 2020 Google LLC
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

package mathlingua.common.chalktalk.phase2

import mathlingua.common.MathLingua
import mathlingua.common.Validation
import mathlingua.common.ValidationSuccess
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase2.ast.Phase2Node
import mathlingua.common.chalktalk.phase2.ast.clause.IdStatement
import mathlingua.common.chalktalk.phase2.ast.toplevel.DefinesGroup
import mathlingua.common.textalk.ExpressionTexTalkNode
import mathlingua.common.textalk.newTexTalkLexer
import mathlingua.common.textalk.newTexTalkParser
import mathlingua.common.transform.expandAsWritten

interface CodeWriter {
    fun append(node: Phase2Node, hasDot: Boolean, indent: Int)
    fun writeHeader(header: String)
    fun writeNewline(count: Int = 1)
    fun writeSpace()
    fun writeDot()
    fun writeComma()
    fun writeIndent(hasDot: Boolean, indent: Int)
    fun writePhase1Node(phase1Node: Phase1Node)
    fun writeId(id: IdStatement)
    fun writeStatement(stmtText: String, root: Validation<ExpressionTexTalkNode>)
    fun writeIdentifier(name: String, isVarArgs: Boolean)
    fun writeText(text: String)
    fun writeDirect(text: String)
    fun beginTopLevel()
    fun endTopLevel()
    fun newCodeWriter(defines: List<DefinesGroup>): CodeWriter
    fun getCode(): String
}

open class HtmlCodeWriter(val defines: List<DefinesGroup>) : CodeWriter {
    protected val builder = StringBuilder()

    override fun append(node: Phase2Node, hasDot: Boolean, indent: Int) {
        builder.append(node.toCode(hasDot, indent, newCodeWriter(defines)).getCode())
    }

    override fun writeHeader(header: String) {
        builder.append("<span class='mathlingua-header'>")
        builder.append(header)
        builder.append(':')
        builder.append("</span>")
    }

    override fun writeNewline(count: Int) {
        for (i in 0 until count) {
            builder.append("<br/>")
        }
    }

    override fun writeSpace() {
        builder.append("<span class='mathlingua-whitespace'></span>")
    }

    override fun writeDot() {
        builder.append("<span class='mathlingua-dot'>")
        builder.append('.')
        builder.append("</span>")
    }

    override fun writeComma() {
        builder.append("<span class='mathlingua-comma'>")
        builder.append(',')
        builder.append("</span>")
    }

    override fun writeIndent(hasDot: Boolean, indent: Int) {
        for (i in 0 until indent - 2) {
            writeSpace()
        }
        if (indent - 2 >= 0) {
            if (hasDot) {
                writeDot()
            } else {
                writeSpace()
            }
        }
        if (indent - 1 >= 0) {
            writeSpace()
        }
    }

    override fun writePhase1Node(phase1Node: Phase1Node) {
        builder.append("<span class='mathlingua-argument'>")
        val code = prettyPrintIdentifier(phase1Node.toCode())
        builder.append("\\[${code
                .replace("{", "\\{")
                .replace("}", "\\}")
                // replace _\{...\} with _{...}
                // because that is required to support things
                // like M_{i, j}
                .replace(Regex("_\\\\\\{(.*?)\\\\\\}"), "_{$1}")}\\]")
        builder.append("</span>")
    }

    override fun writeId(id: IdStatement) {
        builder.append("<span class='mathlingua-id'>")
        builder.append('[')
        val stmt = id.toStatement().toCode(false, 0, MathLinguaCodeWriter(emptyList())).getCode()
        builder.append(stmt.removeSurrounding("'", "'"))
        builder.append(']')
        builder.append("</span>")
    }

    override fun writeText(text: String) {
        builder.append("<span class='mathlingua-text'>")
        builder.append('"')
        builder.append(text.replace("&", ""))
        builder.append('"')
        builder.append("</span>")
    }

    override fun writeStatement(stmtText: String, root: Validation<ExpressionTexTalkNode>) {
        builder.append("<span class='mathlingua-statement'>")
        val IS = "is"
        if (stmtText.contains(IS)) {
            val index = stmtText.indexOf(IS)
            builder.append("\\[${stmtText.substring(0, index)}\\]")
            writeSpace()
            writeDirect("<span class='mathlingua-is'>is</span>")
            writeSpace()
            val lhs = stmtText.substring(index + IS.length).trim()
            val lhsParsed = newTexTalkParser().parse(newTexTalkLexer(lhs))
            if (lhsParsed.errors.isEmpty()) {
                val patternsToWrittenAs = MathLingua.getPatternsToWrittenAs(defines)
                builder.append("\\[${expandAsWritten(lhsParsed.root, patternsToWrittenAs)}\\]")
            } else {
                writeDirect(lhs)
            }
        } else {
            if (root is ValidationSuccess && defines.isNotEmpty()) {
                val patternsToWrittenAs = MathLingua.getPatternsToWrittenAs(defines)
                builder.append("\\[${expandAsWritten(root.value, patternsToWrittenAs)}\\]")
            } else {
                builder.append("\\[$stmtText\\]")
            }
        }
        builder.append("</span>")
    }

    override fun writeIdentifier(name: String, isVarArgs: Boolean) {
        builder.append("<span class='mathlingua-identifier'>\\[")
        builder.append(prettyPrintIdentifier(name))
        if (isVarArgs) {
            builder.append("...")
        }
        builder.append("\\]</span>")
    }

    override fun writeDirect(text: String) {
        builder.append(text)
    }

    override fun beginTopLevel() {
        builder.append("<span class='mathlingua-top-level'>")
    }

    override fun endTopLevel() {
        builder.append("</span>")
    }

    override fun newCodeWriter(defines: List<DefinesGroup>) = HtmlCodeWriter(defines)

    override fun getCode(): String {
        val text = builder.toString()
                .replace(Regex("(\\s*<\\s*br\\s*/\\s*>\\s*)+$"), "")
                .replace(Regex("^(\\s*<\\s*br\\s*/\\s*>\\s*)+$"), "")
        return "<span class='mathlingua'>$text</span>"
    }
}

class MathLinguaCodeWriter(val defines: List<DefinesGroup>) : CodeWriter {
    private val builder = StringBuilder()

    override fun append(node: Phase2Node, hasDot: Boolean, indent: Int) {
        builder.append(node.toCode(hasDot, indent, newCodeWriter(defines)).getCode())
    }

    override fun writeHeader(header: String) {
        builder.append(header)
        builder.append(':')
    }

    override fun writeNewline(count: Int) {
        for (i in 0 until count) {
            builder.append('\n')
        }
    }

    override fun writeSpace() {
        builder.append(' ')
    }

    override fun writeDot() {
        builder.append('.')
    }

    override fun writeComma() {
        builder.append(',')
    }

    override fun writeIndent(hasDot: Boolean, indent: Int) {
        for (i in 0 until indent - 2) {
            writeSpace()
        }
        if (indent - 2 >= 0) {
            if (hasDot) {
                writeDot()
            } else {
                writeSpace()
            }
        }
        if (indent - 1 >= 0) {
            writeSpace()
        }
    }

    override fun writePhase1Node(phase1Node: Phase1Node) {
        builder.append(phase1Node.toCode())
    }

    override fun writeId(id: IdStatement) {
        builder.append('[')
        val stmt = id.toStatement().toCode(false, 0, newCodeWriter(emptyList())).getCode()
        builder.append(stmt.removeSurrounding("'", "'"))
        builder.append(']')
    }

    override fun writeText(text: String) {
        builder.append('"')
        builder.append(text)
        builder.append('"')
    }

    override fun writeStatement(stmtText: String, root: Validation<ExpressionTexTalkNode>) {
        if (root is ValidationSuccess && defines.isNotEmpty()) {
            val patternsToWrittenAs = MathLingua.getPatternsToWrittenAs(defines)
            builder.append("'${expandAsWritten(root.value, patternsToWrittenAs)}'")
        } else {
            builder.append("'$stmtText'")
        }
    }

    override fun writeIdentifier(name: String, isVarArgs: Boolean) {
        builder.append(name)
        if (isVarArgs) {
            builder.append("...")
        }
    }

    override fun writeDirect(text: String) {
        builder.append(text)
    }

    override fun beginTopLevel() {}

    override fun endTopLevel() {}

    override fun newCodeWriter(defines: List<DefinesGroup>) = MathLinguaCodeWriter(defines)

    override fun getCode() = builder.toString()
}

internal fun prettyPrintIdentifier(text: String): String {
    val regex = Regex("([a-zA-Z]+)([0-9]+)")
    val match = regex.find(text)
    return if (match != null) {
        val groups = match.groupValues
        val name = if (isGreekLetter(groups[1])) {
            "\\${groups[1]}"
        } else {
            groups[1]
        }
        val number = groups[2]
        "${name}_$number"
    } else {
        if (isGreekLetter(text)) {
            "\\$text"
        } else {
            text
        }
    }
}

// ------------------------------------------------------------------------------------------------------------------ //

private fun isGreekLetter(letter: String) =
        mutableSetOf(
                "alpha",
                "beta",
                "gamma",
                "delta",
                "epsilon",
                "zeta",
                "eta",
                "theta",
                "iota",
                "kappa",
                "lambda",
                "mu",
                "nu",
                "xi",
                "omicron",
                "pi",
                "rho",
                "sigma",
                "tau",
                "upsilon",
                "phi",
                "chi",
                "psi",
                "omega",
                "Alpha",
                "Beta",
                "Gamma",
                "Delta",
                "Epsilon",
                "Zeta",
                "Eta",
                "Theta",
                "Iota",
                "Kappa",
                "Lambda",
                "Mu",
                "Nu",
                "Xi",
                "Omicron",
                "Pi",
                "Rho",
                "Sigma",
                "Tau",
                "Upsilon",
                "Phi",
                "Chi",
                "Psi",
                "Omega").contains(letter)
