@file:Suppress("RegExpRedundantEscape")

package de.robolab.server.auth.parser

import de.robolab.common.utils.Logger
import de.robolab.server.auth.Permission
import de.robolab.server.externaljs.env

sealed class RoleFileInstruction {

    companion object :
        DelegateParser<RoleFileInstruction>(Comment + ClassDeclaration + PermissionInstruction + ProviderPermission + ProviderDeclaration) {
        private val paramRegex = """(?<!\\)((?:\\\\)*)\$(\w+)""".toRegex()
        fun String.replaceParamPlaceholders(params: List<String> = emptyList()): String {
            return replace(paramRegex) { match ->
                val index: Int? = match.groupValues[2].toIntOrNull()
                match.groupValues[1] + if (index == null || index >= params.size) {
                    env[match.groupValues[2]] as? String
                        ?: throw IllegalArgumentException("Env-Variable not found or has unexpected (non-string) value (name:\"${match.groupValues[2]}\", value:${env[match.groupValues[2]]}")
                } else {
                    params[index]
                }
            }
        }
    }


    class Comment(val comment: String) : RoleFileInstruction() {

        companion object : RegexParser<Comment>() {
            override val regex: Regex = """#(.*)""".toRegex()

            override fun parse(match: MatchResult): Comment = Comment(match.groupValues[1])

        }
    }

    sealed class ClassDeclaration : RoleFileInstruction() {

        abstract fun tryMatch(input: String): List<String>?

        companion object : DelegateParser<ClassDeclaration>(RegexClassDeclaration + LiteralClassDeclaration)

        class RegexClassDeclaration(val regex: Regex) : ClassDeclaration() {

            override fun tryMatch(input: String): List<String>? =
                regex.matchEntire(input)?.groupValues

            companion object : RegexParser<RegexClassDeclaration>() {
                override val regex: Regex = """\|\/([^|]+)\/\|""".toRegex()

                override fun parse(match: MatchResult): RegexClassDeclaration =
                    RegexClassDeclaration(match.groupValues[1].toRegex())
            }
        }

        class LiteralClassDeclaration(val literal: String) : ClassDeclaration() {

            override fun tryMatch(input: String): List<String>? =
                if (input == literal) emptyList() else null

            companion object : RegexParser<LiteralClassDeclaration>() {
                override val regex: Regex = """\|([^|\/](?:[^|]*[^|\/])?)\|""".toRegex()

                override fun parse(match: MatchResult): LiteralClassDeclaration =
                    LiteralClassDeclaration(match.groupValues[1])
            }
        }
    }

    sealed class PermissionInstruction : RoleFileInstruction() {

        companion object :
            DelegateParser<PermissionInstruction>(ClassPermissionInstruction + LiteralPermissionInstruction)

        class LiteralPermissionInstruction(val permissionName: String, val parameters: List<String>) :
            PermissionInstruction() {
            fun resolveParams(params: List<String>): Permission =
                Permission(permissionName, parameters.map { param ->
                    param.replaceParamPlaceholders(params)
                })

            companion object : RegexParser<LiteralPermissionInstruction>() {

                override val regex: Regex = """\+(\w+)(\(.*\))?""".toRegex()

                override fun parse(match: MatchResult): LiteralPermissionInstruction {
                    return LiteralPermissionInstruction(
                        match.groupValues[1],
                        match.groupValues[2].split(',').map(String::trim)
                    )
                }
            }
        }

        class ClassPermissionInstruction(val className: String) : PermissionInstruction() {

            fun resolveParams(params: List<String>) =
                ClassPermissionInstruction(className.replaceParamPlaceholders(params))

            companion object : RegexParser<ClassPermissionInstruction>() {
                override val regex: Regex = """\+\|([^|](?:[^|]*[^|])?)\|""".toRegex()

                private val logger = Logger("ClassPermissionInstruction")

                override fun parse(match: MatchResult): ClassPermissionInstruction {
                    val name = match.groupValues[1]
                    if (name.startsWith("/") && name.endsWith("/"))
                        logger.w(
                            "Literal name starts and ends with '/' (of \"$name\"). Note that ClassPermissionInstructions only accept literals and not regex (literals are matched against class-regex automatically)"
                        )
                    return ClassPermissionInstruction(name)
                }
            }
        }
    }

    class ProviderDeclaration(val name: String, val parameters: List<String>) : RoleFileInstruction() {
        companion object : RegexParser<ProviderDeclaration>() {
            override val regex: Regex = """\@(\w+)(?:\s*\((.*)\))?""".toRegex()
            override fun parse(match: MatchResult): ProviderDeclaration {
                return ProviderDeclaration(match.groupValues[1], match.groupValues[2].split(',').map(String::trim))
            }
        }
    }

    sealed class ProviderPermission(val permission: PermissionInstruction) : RoleFileInstruction() {

        companion object : DelegateParser<ProviderPermission>(ProviderPredicatePermission + UserIdentifierPermission)

        class ProviderPredicatePermission(
            val predicateName: String,
            val predicateParameters: List<String>,
            permission: PermissionInstruction
        ) : ProviderPermission(permission) {
            companion object : DelegateParser<ProviderPredicatePermission>((object :
                RegexChainParser<PermissionInstruction, ProviderPredicatePermission>() {
                override val regex: Regex = """\?\w+(?:\((.*(?<!\\)(?:\\\\)*)\))?:\s*(\S.*)""".toRegex()
                override val subParseGroupIndex: Int = 2

                override fun merge(state: MatchResult, result: PermissionInstruction): ProviderPredicatePermission {
                    return ProviderPredicatePermission(
                        state.groupValues[1],
                        state.groupValues[2].split(',').map(String::trim),
                        result
                    )
                }

            }) * PermissionInstruction)
        }

        sealed class UserIdentifierPermission(permission: PermissionInstruction) :
            ProviderPermission(permission) {

            companion object :
                DelegateParser<UserIdentifierPermission>(LiteralUserIdentifierPermission + RegexUserIdentifierPermission)

            abstract fun matchesUser(name: String): List<String>?

            class LiteralUserIdentifierPermission(val userIdentifier: String, permission: PermissionInstruction) :
                UserIdentifierPermission(permission) {

                companion object :
                    DelegateParser<UserIdentifierPermission>((object :
                        RegexChainParser<PermissionInstruction, UserIdentifierPermission>() {
                        override val regex: Regex = """(\w+):\s*(\S.*)""".toRegex()
                        override val subParseGroupIndex: Int = 2

                        override fun merge(
                            state: MatchResult,
                            result: PermissionInstruction
                        ): UserIdentifierPermission {
                            return LiteralUserIdentifierPermission(state.groupValues[1], result)
                        }

                    }) * PermissionInstruction)

                override fun matchesUser(name: String): List<String>? =
                    if (name == userIdentifier) emptyList() else null
            }

            class RegexUserIdentifierPermission(val userIdentifierRegex: Regex, permission: PermissionInstruction) :
                UserIdentifierPermission(permission) {


                companion object :
                    DelegateParser<UserIdentifierPermission>((object :
                        RegexChainParser<PermissionInstruction, UserIdentifierPermission>() {
                        override val regex: Regex = """\/((?:\\?[^\\\n\/]+|\\\/|\\\\)+)\/:\s*(\S.*)""".toRegex()
                        override val subParseGroupIndex: Int = 2

                        override fun merge(
                            state: MatchResult,
                            result: PermissionInstruction
                        ): UserIdentifierPermission {
                            return RegexUserIdentifierPermission(state.groupValues[1].toRegex(), result)
                        }

                    }) * PermissionInstruction)

                override fun matchesUser(name: String): List<String>? =
                    userIdentifierRegex.matchEntire(name)?.groupValues?.let { it.subList(1, it.size) }
            }
        }
    }
}