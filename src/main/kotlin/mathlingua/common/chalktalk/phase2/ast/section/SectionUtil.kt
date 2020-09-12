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

package mathlingua.common.chalktalk.phase2.ast.section

import mathlingua.common.MutableLocationTracker
import mathlingua.common.ParseError
import mathlingua.common.Validation
import mathlingua.common.ValidationFailure
import mathlingua.common.ValidationSuccess
import mathlingua.common.chalktalk.phase1.ast.Phase1Node
import mathlingua.common.chalktalk.phase1.ast.Section
import mathlingua.common.chalktalk.phase1.ast.getColumn
import mathlingua.common.chalktalk.phase1.ast.getRow
import mathlingua.common.chalktalk.phase2.*
import mathlingua.common.chalktalk.phase2.ast.Phase2Node
import mathlingua.common.chalktalk.phase2.ast.clause.*
import mathlingua.common.chalktalk.phase2.ast.clause.Target
import mathlingua.common.validationFailure
import mathlingua.common.validationSuccess

private fun canBeOnOneLine(target: Target) =
        target is Identifier ||
        target is TupleNode ||
        target is AbstractionNode ||
        target is AssignmentNode

fun appendTargetArgs(writer: CodeWriter, targets: List<Target>, indent: Int) {
    var i = 0
    while (i < targets.size) {
        val lineItems = mutableListOf<Target>()
        while (i < targets.size && canBeOnOneLine(targets[i])) {
            lineItems.add(targets[i++])
        }
        if (lineItems.isEmpty()) {
            writer.writeNewline()
            writer.append(targets[i++], true, indent)
        } else {
            writer.writeSpace()
            for (j in lineItems.indices) {
                writer.append(lineItems[j], false, 0)
                if (j != lineItems.size - 1) {
                    writer.writeComma()
                    writer.writeSpace()
                }
            }
        }
    }
}

fun <T : Phase2Node> validateEmptySection(
    rawNode: Phase1Node,
    tracker: MutableLocationTracker,
    name: String,
    build: () -> T
): Validation<T> {
    val node = rawNode.resolve()
    val row = getRow(node)
    val column = getColumn(node)

    val errors = ArrayList<ParseError>()
    if (node !is Section) {
        errors.add(
            ParseError(
                "Expected a Section",
                row, column
            )
        )
    }

    val sect = node as Section
    if (sect.name.text != name) {
        errors.add(
            ParseError(
                "Expected a Section with name '$name' but found " + sect.name.text,
                row, column
            )
        )
    }

    return if (errors.isNotEmpty()) {
        validationFailure(errors)
    } else {
        validationSuccess(
            tracker,
            rawNode,
            build()
        )
    }
}

fun <T : Phase2Node> validateStatementSection(
    rawNode: Phase1Node,
    tracker: MutableLocationTracker,
    name: String,
    build: (statement: Statement) -> T
): Validation<T> {
    val node = rawNode.resolve()
    val row = getRow(node)
    val column = getColumn(node)

    val errors = ArrayList<ParseError>()
    if (node !is Section) {
        errors.add(
            ParseError(
                "Expected a Section",
                row, column
            )
        )
    }

    val sect = node as Section
    if (sect.name.text != name) {
        errors.add(
            ParseError(
                "Expected a Section with name '$name' but found " + sect.name.text,
                row, column
            )
        )
    }

    var arg: Statement? = null
    if (sect.args.size != 1) {
        errors.add(
            ParseError(
                "Expected a single argument but found ${sect.args.size}",
                row, column
            )
        )
    } else {
        when (val validation = validateStatement(sect.args[0], tracker)) {
            is ValidationSuccess -> arg = validation.value
            is ValidationFailure -> errors.addAll(validation.errors)
        }
    }

    return if (errors.isNotEmpty()) {
        validationFailure(errors)
    } else {
        validationSuccess(
            tracker,
            rawNode,
            build(arg!!)
        )
    }
}
