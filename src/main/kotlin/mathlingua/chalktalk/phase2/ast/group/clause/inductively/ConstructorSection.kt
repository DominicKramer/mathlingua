/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mathlingua.chalktalk.phase2.ast.group.clause.inductively

import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.CodeWriter
import mathlingua.chalktalk.phase2.ast.DEFAULT_CONSTRUCTOR_SECTION
import mathlingua.chalktalk.phase2.ast.clause.Target
import mathlingua.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.chalktalk.phase2.ast.neoTrack
import mathlingua.chalktalk.phase2.ast.neoValidateTargetSection
import mathlingua.chalktalk.phase2.ast.section.appendTargetArgs
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError

data class ConstructorSection(val targets: List<Target>) : Phase2Node {
    override fun forEach(fn: (node: Phase2Node) -> Unit) = targets.forEach(fn)

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter): CodeWriter {
        writer.writeIndent(isArg, indent)
        writer.writeHeader("constructor")
        appendTargetArgs(writer, targets, indent + 2)
        return writer
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(
            ConstructorSection(targets = targets.map { it.transform(chalkTransformer) as Target }))
}

fun neoValidateConstructorSection(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    neoTrack(node, tracker) {
        neoValidateTargetSection(
            node.resolve(),
            errors,
            "constructor",
            DEFAULT_CONSTRUCTOR_SECTION,
            tracker,
            ::ConstructorSection)
    }