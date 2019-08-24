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

package mathlingua.common.textalk

enum class NodeType {
    Token,
    Identifier,
    Operator,
    ParenGroup,
    SquareGroup,
    CurlyGroup,
    NamedGroup,
    Command,
    CommandPart,
    Expression,
    SubSup,
    Parameters,
    Comma,
    Is
}

interface Node {
    val type: NodeType
    fun toCode(): String
    fun forEach(fn: (node: Node) -> Unit)
}

data class IsNode(val lhs: ParametersNode, val rhs: ParametersNode) : Node {
    override val type: NodeType
        get() = NodeType.Is

    override fun toCode(): String {
        val builder = StringBuilder()
        builder.append(lhs.toCode())
        builder.append(" is ")
        builder.append(rhs.toCode())
        return builder.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        fn(lhs)
        fn(rhs)
    }
}

data class CommandPart(
    val name: TextNode,
    val square: GroupNode?,
    val subSup: SubSupNode?,
    val groups: List<GroupNode>,
    val namedGroups: List<NamedGroupNode>
) : Node {
    override val type: NodeType
        get() = NodeType.CommandPart

    override fun toCode(): String {
        val buffer = StringBuilder()

        buffer.append(name.toCode())

        if (square != null) {
            buffer.append(square.toCode())
        }

        if (subSup != null) {
            buffer.append(subSup.toCode())
        }

        for (grp in groups) {
            buffer.append(grp.toCode())
        }

        if (namedGroups.isNotEmpty()) {
            buffer.append(":")
        }
        for (namedGrp in namedGroups) {
            buffer.append(namedGrp.toCode())
        }
        return buffer.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        fn(name)

        if (square != null) {
            fn(square)
        }

        if (subSup != null) {
            fn(subSup)
        }

        groups.forEach(fn)
        namedGroups.forEach(fn)
    }
}

data class Command(val parts: List<CommandPart>) : Node {
    override val type: NodeType
        get() = NodeType.Command

    override fun toCode(): String {
        val builder = StringBuilder("\\")
        for (i in 0 until parts.size) {
            if (i > 0) {
                builder.append('.')
            }
            builder.append(parts[i].toCode())
        }
        return builder.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        parts.forEach(fn)
    }
}

data class ExpressionNode(val children: List<Node>) : Node {

    override val type: NodeType
        get() = NodeType.Expression

    override fun toCode(): String {
        val builder = StringBuilder()
        val children = children
        for (i in children.indices) {
            val child = children[i]
            builder.append(child.toCode())
            if (i != children.size - 1) {
                builder.append(" ")
            }
        }
        return builder.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        children.forEach(fn)
    }
}

data class ParametersNode(val items: List<ExpressionNode>) : Node {
    override val type: NodeType
        get() = NodeType.Parameters

    override fun toCode(): String {
        val buffer = StringBuilder()

        if (items.isNotEmpty()) {
            buffer.append(items[0].toCode())
        }

        for (i in 1 until items.size) {
            buffer.append(", ")
            buffer.append(items[i].toCode())
        }

        return buffer.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        items.forEach(fn)
    }
}

data class GroupNode(override val type: NodeType, val parameters: ParametersNode) : Node {

    override fun toCode(): String {
        val prefix: String
        val suffix: String

        when (type) {
            NodeType.ParenGroup -> {
                prefix = "("
                suffix = ")"
            }
            NodeType.SquareGroup -> {
                prefix = "["
                suffix = "]"
            }
            NodeType.CurlyGroup -> {
                prefix = "{"
                suffix = "}"
            }
            else -> throw RuntimeException("Unrecognized group type $type")
        }

        val buffer = StringBuilder(prefix)
        buffer.append(parameters.toCode())
        buffer.append(suffix)
        return buffer.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        fn(parameters)
    }
}

data class NamedGroupNode(
    val name: TextNode,
    val group: GroupNode
) : Node {

    override val type: NodeType
        get() = NodeType.NamedGroup

    override fun toCode(): String {
        val buffer = StringBuilder()
        buffer.append(name.toCode())
        buffer.append(group.toCode())
        return buffer.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        fn(name)
        fn(group)
    }
}

data class SubSupNode(
    val sub: GroupNode?,
    val sup: GroupNode?
) : Node {

    override val type: NodeType
        get() = NodeType.SubSup

    override fun toCode(): String {
        val builder = StringBuilder()
        if (sub != null) {
            builder.append("_")
            builder.append(sub.toCode())
        }

        if (sup != null) {
            builder.append("^")
            builder.append(sup.toCode())
        }
        return builder.toString()
    }

    override fun forEach(fn: (node: Node) -> Unit) {
        if (sub != null) {
            fn(sub)
        }

        if (sup != null) {
            fn(sup)
        }
    }
}

data class TextNode(override val type: NodeType, val text: String) : Node {
    override fun toCode(): String {
        return text
    }

    override fun forEach(fn: (node: Node) -> Unit) {
    }
}

data class TexTalkToken(
    val text: String,
    val tokenType: TexTalkTokenType,
    val row: Int,
    val column: Int
) : Node {
    override val type: NodeType
        get() = NodeType.Token

    override fun toCode(): String {
        return text
    }

    override fun forEach(fn: (node: Node) -> Unit) {
    }
}

enum class TexTalkTokenType {
    Backslash,
    LParen,
    RParen,
    LSquare,
    RSquare,
    LCurly,
    RCurly,
    Operator,
    Identifier,
    Comma,
    Period,
    Colon,
    Underscore,
    Caret,
    Invalid
}