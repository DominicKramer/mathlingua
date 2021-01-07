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

package mathlingua.chalktalk.phase2.ast.group.toplevel.defineslike.defines

import mathlingua.chalktalk.phase1.ast.Phase1Node
import mathlingua.chalktalk.phase2.CodeWriter
import mathlingua.chalktalk.phase2.ast.DEFAULT_DEFINES_GENERATED_GROUP
import mathlingua.chalktalk.phase2.ast.DEFAULT_DEFINES_SECTION
import mathlingua.chalktalk.phase2.ast.DEFAULT_GENERATED_SECTION
import mathlingua.chalktalk.phase2.ast.DEFAULT_ID_STATEMENT
import mathlingua.chalktalk.phase2.ast.DEFAULT_WRITTEN_SECTION
import mathlingua.chalktalk.phase2.ast.clause.IdStatement
import mathlingua.chalktalk.phase2.ast.clause.sectionsMatchNames
import mathlingua.chalktalk.phase2.ast.common.Phase2Node
import mathlingua.chalktalk.phase2.ast.group.toplevel.defineslike.WrittenSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.defineslike.validateWrittenSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.UsingSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.WhenSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.WhereSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.metadata.section.MetaDataSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.metadata.section.validateMetaDataSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.validateUsingSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.validateWhenSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.shared.validateWhereSection
import mathlingua.chalktalk.phase2.ast.group.toplevel.topLevelToCode
import mathlingua.chalktalk.phase2.ast.neoGetId
import mathlingua.chalktalk.phase2.ast.neoTrack
import mathlingua.chalktalk.phase2.ast.section.neoEnsureNonNull
import mathlingua.chalktalk.phase2.ast.section.neoIdentifySections
import mathlingua.chalktalk.phase2.ast.section.neoIfNonNull
import mathlingua.chalktalk.phase2.ast.validateGroup
import mathlingua.support.MutableLocationTracker
import mathlingua.support.ParseError
import mathlingua.transform.signature

data class DefinesGeneratedGroup(
    override val signature: String?,
    override val id: IdStatement,
    override val definesSection: DefinesSection,
    val whereSection: WhereSection?,
    val whenSection: WhenSection?,
    val generatedSection: GeneratedSection,
    val usingSection: UsingSection?,
    override val writtenSection: WrittenSection,
    override val metaDataSection: MetaDataSection?
) : DefinesGroup(metaDataSection) {

    override fun forEach(fn: (node: Phase2Node) -> Unit) {
        fn(id)
        fn(definesSection)
        if (whereSection != null) {
            fn(whereSection)
        }
        if (whenSection != null) {
            fn(whenSection)
        }
        fn(generatedSection)
        if (usingSection != null) {
            fn(usingSection)
        }
        fn(writtenSection)
        if (metaDataSection != null) {
            fn(metaDataSection)
        }
    }

    override fun toCode(isArg: Boolean, indent: Int, writer: CodeWriter): CodeWriter {
        val sections =
            mutableListOf(
                definesSection,
                whereSection,
                whenSection,
                generatedSection,
                writtenSection,
                metaDataSection)
        return topLevelToCode(writer, isArg, indent, id, *sections.toTypedArray())
    }

    override fun transform(chalkTransformer: (node: Phase2Node) -> Phase2Node) =
        chalkTransformer(
            DefinesGeneratedGroup(
                signature = signature,
                id = id.transform(chalkTransformer) as IdStatement,
                definesSection = definesSection.transform(chalkTransformer) as DefinesSection,
                whereSection = whereSection?.transform(chalkTransformer) as WhereSection?,
                whenSection = whenSection?.transform(chalkTransformer) as WhenSection?,
                generatedSection = generatedSection.transform(chalkTransformer) as GeneratedSection,
                usingSection = usingSection?.transform(chalkTransformer) as UsingSection?,
                writtenSection = writtenSection.transform(chalkTransformer) as WrittenSection,
                metaDataSection = metaDataSection?.transform(chalkTransformer) as MetaDataSection?))
}

fun isDefinesGeneratedGroup(node: Phase1Node) = sectionsMatchNames(node, "Defines", "generated")

fun validateDefinesGeneratedGroup(
    node: Phase1Node, errors: MutableList<ParseError>, tracker: MutableLocationTracker
) =
    neoTrack(node, tracker) {
        validateGroup(node.resolve(), errors, "Defines", DEFAULT_DEFINES_GENERATED_GROUP) { group ->
            neoIdentifySections(
                group,
                errors,
                DEFAULT_DEFINES_GENERATED_GROUP,
                listOf(
                    "Defines", "where?", "when?", "generated", "using?", "written", "Metadata?")) {
            sections ->
                val id = neoGetId(group, errors, DEFAULT_ID_STATEMENT, tracker)
                DefinesGeneratedGroup(
                    signature = id.signature(),
                    id = id,
                    definesSection =
                        neoEnsureNonNull(sections["Defines"], DEFAULT_DEFINES_SECTION) {
                            validateDefinesSection(it, errors, tracker)
                        },
                    whereSection =
                        neoIfNonNull(sections["where"]) {
                            validateWhereSection(it, errors, tracker)
                        },
                    whenSection =
                        neoIfNonNull(sections["when"]) { validateWhenSection(it, errors, tracker) },
                    generatedSection =
                        neoEnsureNonNull(sections["generated"], DEFAULT_GENERATED_SECTION) {
                            validateGeneratedSection(it, errors, tracker)
                        },
                    usingSection =
                        neoIfNonNull(sections["using"]) {
                            validateUsingSection(it, errors, tracker)
                        },
                    writtenSection =
                        neoEnsureNonNull(sections["written"], DEFAULT_WRITTEN_SECTION) {
                            validateWrittenSection(it, errors, tracker)
                        },
                    metaDataSection =
                        neoIfNonNull(sections["Metadata"]) {
                            validateMetaDataSection(it, errors, tracker)
                        })
            }
        }
    }
