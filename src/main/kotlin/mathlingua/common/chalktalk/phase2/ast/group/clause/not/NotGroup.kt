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

package mathlingua.common.chalktalk.phase2.ast.group.clause.not

import mathlingua.common.support.MutableLocationTracker
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase2.ast.common.OnePartNode
import mathlingua.common.chalktalk.phase2.ast.clause.Clause
import mathlingua.common.chalktalk.phase2.ast.clause.firstSectionMatchesName
import mathlingua.common.chalktalk.phase2.ast.clause.validateSingleSectionGroup

data class NotGroup(val notSection: NotSection) :
    OnePartNode<NotSection>(
        notSection,
        ::NotGroup
    ), Clause

fun isNotGroup(node: Phase1Node) = firstSectionMatchesName(node, "not")

fun validateNotGroup(node: Phase1Node, tracker: MutableLocationTracker) = validateSingleSectionGroup(
        tracker,
        node, "not", ::NotGroup,
        ::validateNotSection
)