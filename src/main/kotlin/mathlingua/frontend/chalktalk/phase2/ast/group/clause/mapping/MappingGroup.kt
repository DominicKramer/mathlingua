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

package mathlingua.frontend.chalktalk.phase2.ast.group.clause.mapping

import mathlingua.frontend.chalktalk.phase1.ast.Phase1Node
import mathlingua.frontend.chalktalk.phase2.CodeWriter
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_AS_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_FROM_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_MAPPING_GROUP
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_MAPPING_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_TO_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.clause.Clause
import mathlingua.frontend.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.frontend.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.frontend.chalktalk.phase2.ast.group.clause.expands.AsSection
import mathlingua.frontend.chalktalk.phase2.ast.group.clause.expands.validateAsSection
import mathlingua.frontend.chalktalk.phase2.ast.section.ensureNonNull
import mathlingua.frontend.chalktalk.phase2.ast.section.identifySections
import mathlingua.frontend.chalktalk.phase2.ast.track
import mathlingua.frontend.chalktalk.phase2.ast.validateGroup
import mathlingua.frontend.support.MutableLocationTracker
import mathlingua.frontend.support.ParseError

data class MappingGroup(
    val mappingSection: MappingSection,
    val fromSection: FromSection,
    val thenSection: ToSection,
    val asSection: AsSection
) : Clause {
    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(mappingSection)
        fn(fromSection)
        fn(thenSection)
        fn(asSection)
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter) =
        mathlingua.frontend.chalktalk.phase2.ast.clause.toCode(
            writer, isArg, indent, mappingSection, fromSection, thenSection, asSection)

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(
            MappingGroup(
                mappingSection = mappingSection.transform(chalkTransformer) as MappingSection,
                fromSection = fromSection.transform(chalkTransformer) as FromSection,
                thenSection = thenSection.transform(chalkTransformer) as ToSection,
                asSection = asSection.transform(chalkTransformer) as AsSection))
}

fun isMappingGroup(node: Phase1Node) = firstSectionMatchesName(node, "mapping")

fun validateMappingGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    track(node, tracker) {
        validateGroup(node.resolve(), errors, "mapping", DEFAULT_MAPPING_GROUP) { group ->
            identifySections(
                group, errors, DEFAULT_MAPPING_GROUP, listOf("mapping", "from", "to", "as")) {
            sections ->
                MappingGroup(
                    mappingSection =
                        ensureNonNull(sections["mapping"], DEFAULT_MAPPING_SECTION) {
                            validateMappingSection(it, errors, tracker)
                        },
                    fromSection =
                        ensureNonNull(sections["from"], DEFAULT_FROM_SECTION) {
                            validateFromSection(it, errors, tracker)
                        },
                    thenSection =
                        ensureNonNull(sections["to"], DEFAULT_TO_SECTION) {
                            validateToSection(it, errors, tracker)
                        },
                    asSection =
                        ensureNonNull(sections["as"], DEFAULT_AS_SECTION) {
                            validateAsSection(it, errors, tracker)
                        })
            }
        }
    }
