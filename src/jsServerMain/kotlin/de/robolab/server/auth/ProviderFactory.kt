package de.robolab.server.auth

import de.robolab.server.auth.parser.PermissionMap
import de.robolab.server.auth.parser.RoleFileInstruction
import de.robolab.server.net.Client

object ProviderFactory {

    private val TEMPLATES: Map<String, IdentityProviderTemplate<*>> =
        listOf<IdentityProviderTemplate<*>>().associateBy { it.name.toLowerCase() }


    private abstract class IdentityProviderTemplate<T>(val name: String) {
        abstract fun create(params: List<String>): IdentityProvider<T>
    }

    private fun getIdentityProvider(decl: RoleFileInstruction.ProviderDeclaration): IdentityProvider<*> {
        return (TEMPLATES[decl.name.toLowerCase()]
            ?: throw IllegalArgumentException("Auth-Provider not found: \"${decl.name}\"")).create(decl.parameters)
    }

    fun create(
        decl: RoleFileInstruction.ProviderDeclaration,
        perms: List<RoleFileInstruction.ProviderPermission>,
        map: PermissionMap
    ): IAuthProvider = getIdentityProvider(decl).withProviderPermissions(perms, map)

    private fun <T> IdentityProvider<T>.withProviderPermissions(
        perms: List<RoleFileInstruction.ProviderPermission>,
        permMap: PermissionMap
    ): IAuthProvider {
        return object : IAuthProvider {

            val constructedPredicates: List<Pair<suspend (T) -> List<String>?, RoleFileInstruction.PermissionInstruction>> =
                perms.map { perm ->
                    val predicate: (suspend (T) -> List<String>?) = when (perm) {
                        is RoleFileInstruction.ProviderPermission.UserIdentifierPermission -> ({
                            perm.matchesUser(getUsername(it))
                        })
                        is RoleFileInstruction.ProviderPermission.ProviderPredicatePermission -> {
                            val pred: (suspend (T, List<String>) -> Boolean) =
                                predicates[perm.predicateName]
                                    ?: throw IllegalArgumentException("Could not find predicate named \"${perm.predicateName}\"")
                            ({ if (pred(it, perm.predicateParameters)) emptyList() else null })
                        }
                    }
                    predicate to perm.permission
                }

            override suspend fun auth(client: Client, asAdmin: Boolean): User {
                val identity: T = makeIdentity(client, asAdmin)

                @Suppress("UselessCallOnCollection") //Inspection tries to replace "mapNotNull" with "map" which results in type-errors (nulls from "let" are not skipped)
                val permissions = constructedPredicates.mapNotNull { (pred, perm) ->
                    pred(identity).let { permMap.resolvePermissionDeclaration(perm, it ?: return@let null) }
                }.reduce { a, b -> a + b }
                return User(permissions)
            }

        }
    }
}

interface IdentityProvider<T> {
    val predicates: Map<String, suspend (T, List<String>) -> Boolean>
    suspend fun makeIdentity(client: Client, asAdmin: Boolean): T
    suspend fun getUsername(user: T): String
}