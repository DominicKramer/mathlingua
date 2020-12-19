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

package mathlingua.chalktalk.phase2.ast.group.clause.or

import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.ast.DEFAULT_OR_GROUP
import mathlingua.chalktalk.phase2.ast.DEFAULT_OR_SECTION
import mathlingua.chalktalk.phase2.ast.clause.Clause
import mathlingua.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.chalktalk.phase2.ast.common.OnePartNode
import mathlingua.chalktalk.phase2.ast.neoTrack
import mathlingua.chalktalk.phase2.ast.neoValidateGroup
import mathlingua.chalktalk.phase2.ast.section.neoEnsureNonNull
import mathlingua.chalktalk.phase2.ast.section.neoIdentifySections
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError

data class OrGroup(val orSection: OrSection) : OnePartNode<OrSection>(orSection, ::OrGroup), Clause

fun isOrGroup(node: Phase1Node) = firstSectionMatchesName(node, "or")

fun neoValidateOrGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    neoTrack(node, tracker) {
        neoValidateGroup(node.resolve(), errors, "or", DEFAULT_OR_GROUP) { group ->
            neoIdentifySections(group, errors, DEFAULT_OR_GROUP, listOf("or")) { sections ->
                OrGroup(
                    orSection =
                        neoEnsureNonNull(sections["or"], DEFAULT_OR_SECTION) {
                            neoValidateOrSection(it, errors, tracker)
                        })
            }
        }
    }