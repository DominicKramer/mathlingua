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

package mathlingua.chalktalk.phase2.ast.group.clause.given

import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.CodeWriter
import mathlingua.chalktalk.phase2.ast.DEFAULT_ALL_SECTION
import mathlingua.chalktalk.phase2.ast.clause.Statement
import mathlingua.chalktalk.phase2.ast.clause.validateStatement
import mathlingua.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.chalktalk.phase2.ast.neoTrack
import mathlingua.chalktalk.phase2.ast.validateSection
import mathlingua.chalktalk.phase2.ast.validateSingleArg
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError

data class AllSection(val statement: Statement) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(statement)
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter): CodeWriter {
        writer.writeIndent(isArg, indent)
        writer.writeHeader("where")
        writer.append(statement, false, 1)
        return writer
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(AllSection(statement = statement.transform(chalkTransformer) as Statement))
}

fun validateAllSection(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    neoTrack(node, tracker) {
        validateSection(node.resolve(), errors, "all", DEFAULT_ALL_SECTION) { section ->
            validateSingleArg(section, errors, DEFAULT_ALL_SECTION, "statement") {
                AllSection(statement = validateStatement(it, errors, tracker))
            }
        }
    }
