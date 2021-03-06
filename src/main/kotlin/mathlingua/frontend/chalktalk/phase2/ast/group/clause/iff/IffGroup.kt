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

package mathlingua.frontend.chalktalk.phase2.ast.group.clause.iff

import mathlingua.frontend.chalktalk.phase1.ast.Phase1Node
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_IFF_GROUP
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_IFF_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.DEFAULT_THEN_SECTION
import mathlingua.frontend.chalktalk.phase2.ast.clause.Clause
import mathlingua.frontend.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.frontend.chalktalk.phase2.ast.common.TwoPartNode
import mathlingua.frontend.chalktalk.phase2.ast.group.clause.If.ThenSection
import mathlingua.frontend.chalktalk.phase2.ast.group.clause.If.validateThenSection
import mathlingua.frontend.chalktalk.phase2.ast.section.ensureNonNull
import mathlingua.frontend.chalktalk.phase2.ast.section.identifySections
import mathlingua.frontend.chalktalk.phase2.ast.track
import mathlingua.frontend.chalktalk.phase2.ast.validateGroup
import mathlingua.frontend.support.MutableLocationTracker
import mathlingua.frontend.support.ParseError

data class IffGroup(val iffSection: IffSection, val thenSection: ThenSection) :
    TwoPartNode<IffSection, ThenSection>(iffSection, thenSection, ::IffGroup), Clause

fun isIffGroup(node: Phase1Node) = firstSectionMatchesName(node, "iff")

fun validateIffGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    track(node, tracker) {
        validateGroup(node.resolve(), errors, "iff", DEFAULT_IFF_GROUP) { group ->
            identifySections(group, errors, DEFAULT_IFF_GROUP, listOf("iff", "then")) { sections ->
                IffGroup(
                    iffSection =
                        ensureNonNull(sections["iff"], DEFAULT_IFF_SECTION) {
                            validateIffSection(it, errors, tracker)
                        },
                    thenSection =
                        ensureNonNull(sections["then"], DEFAULT_THEN_SECTION) {
                            validateThenSection(it, errors, tracker)
                        })
            }
        }
    }
