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

package mathlingua.frontend.chalktalk.phase2.ast.group.clause.matching

import mathlingua.frontend.chalktalk.phase1.ast.Phase1Node
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_MATCHING_GROUP
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_MATCHING_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.clause.Clause
import mathlingua.frontend.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.frontend.chalktalk.phase2.ast.common.OnePartNode
import mathlingua.frontend.chalktalk.phase2.ast.section.ensureNonNull
import mathlingua.frontend.chalktalk.phase2.ast.section.identifySections
import mathlingua.frontend.chalktalk.phase2.ast.track
import mathlingua.frontend.chalktalk.phase2.ast.validateGroup
import mathlingua.frontend.support.MutableLocationTracker
import mathlingua.frontend.support.ParseError

data class MatchingGroup(val matchingSection: MatchingSection) :
    OnePartNode<MatchingSection>(matchingSection, ::MatchingGroup), Clause

fun isMatchingGroup(node: Phase1Node) = firstSectionMatchesName(node, "matching")

fun validateMatchingGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    track(node, tracker) {
        validateGroup(node.resolve(), errors, "matching", DEFAULT_MATCHING_GROUP) { group ->
            identifySections(group, errors, DEFAULT_MATCHING_GROUP, listOf("matching")) {
            sections ->
                MatchingGroup(
                    matchingSection =
                        ensureNonNull(sections["matching"], DEFAULT_MATCHING_SECTION) {
                            validateMatchingSection(it, errors, tracker)
                        })
            }
        }
    }