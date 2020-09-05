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

package mathlingua.common.chalktalk.phase2.ast.clause

import mathlingua.common.MutableLocationTracker
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase2.CodeWriter
import mathlingua.common.chalktalk.phase2.ast.Phase2Node
import mathlingua.common.chalktalk.phase2.ast.section.ImportSection
import mathlingua.common.chalktalk.phase2.ast.section.LeanSection
import mathlingua.common.chalktalk.phase2.ast.section.VariableSection
import mathlingua.common.chalktalk.phase2.ast.section.validateImportSection
import mathlingua.common.chalktalk.phase2.ast.section.validateLeanSection
import mathlingua.common.chalktalk.phase2.ast.section.validateVariableSection

data class LeanGroup(
    val leanSection: LeanSection,
    val importSection: ImportSection?,
    val variableSection: VariableSection?
) : Clause {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(leanSection)
        if (importSection != null) {
            fn(importSection)
        }
        if (variableSection != null) {
            fn(variableSection)
        }
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter) =
        toCode(writer, isArg, indent, leanSection, importSection, variableSection)

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(LeanGroup(
            leanSection = leanSection.transform(chalkTransformer) as LeanSection,
            importSection = importSection?.transform(chalkTransformer) as ImportSection?,
            variableSection = variableSection?.transform(chalkTransformer) as VariableSection?
        ))
}

fun isLeanGroup(node: Phase1Node) = firstSectionMatchesName(node, "lean")

fun validateLeanGroup(node: Phase1Node, tracker: MutableLocationTracker) = validateTripleSectionGroup(
    tracker,
    node,
    "lean",
    ::validateLeanSection,
    "import?",
    ::validateImportSection,
    "variable?",
    ::validateVariableSection,
    ::LeanGroup
)