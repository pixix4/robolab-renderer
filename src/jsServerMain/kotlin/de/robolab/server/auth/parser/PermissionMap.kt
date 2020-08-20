package de.robolab.server.auth.parser

import de.robolab.server.auth.Permission


class PermissionMap {

    private val _cached: MutableMap<String, Set<Permission>> = mutableMapOf()
    val cached: Map<String, Set<Permission>>
        get() = _cached.toMap() //`_cached` may change at any time, `cached` creates a frozen clone

    private val _globalPermissions: MutableSet<Permission> = mutableSetOf()
    val globalPermissions: Set<Permission>
        get() = _globalPermissions.toSet()

    private val _permissions: MutableMap<RoleFileInstruction.ClassDeclaration, Set<RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction>> =
        mutableMapOf()
    val permissions: Map<RoleFileInstruction.ClassDeclaration, Set<RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction>>
        get() = _permissions.toMap()

    private val _providers: MutableMap<RoleFileInstruction.ProviderDeclaration, Set<RoleFileInstruction.ProviderPermission>> =
        mutableMapOf()
    val providers: Map<RoleFileInstruction.ProviderDeclaration, Set<RoleFileInstruction.ProviderPermission>>
        get() = _providers.toMap()

    fun lookup(name: String, throwOnGlobalOnly: Boolean = false): Set<Permission> = _cached.getOrPut(name) {
        val nonGlobalPermissions: Set<Permission> = _permissions.mapNotNull { (clazz, permissions) ->
            val match = clazz.tryMatch(name) ?: return@mapNotNull null
            permissions.map { it.resolveParams(match) }.toSet()
        }.reduce { a, b -> a + b }
        if (throwOnGlobalOnly && nonGlobalPermissions.isEmpty()) throw IllegalArgumentException("Only found global permissions for name \"$name\"")
        return@getOrPut _globalPermissions + nonGlobalPermissions
    }

    fun addPermission(permissions: Set<RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction>) {
        _globalPermissions += permissions.map { it.resolveParams(emptyList()) }
    }

    fun addPermission(
        clazz: RoleFileInstruction.ClassDeclaration,
        permissions: Set<RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction>
    ) {
        _permissions[clazz] = _permissions[clazz].orEmpty() + permissions
    }

    fun addProvider(
        provider: RoleFileInstruction.ProviderDeclaration,
        vararg providerPermissions: RoleFileInstruction.ProviderPermission
    ) = addProvider(provider, providerPermissions.toList())

    fun addProvider(
        provider: RoleFileInstruction.ProviderDeclaration,
        providerPermissions: List<RoleFileInstruction.ProviderPermission>
    ) {
        _providers[provider] = _providers.getOrElse(provider, ::emptySet) + providerPermissions
    }

    fun resolvePermissionDeclaration(
        perm: RoleFileInstruction.PermissionInstruction,
        params: List<String>
    ): Set<Permission> = when (perm) {
        is RoleFileInstruction.PermissionInstruction.LiteralPermissionInstruction -> setOf(perm.resolveParams(params))
        is RoleFileInstruction.PermissionInstruction.ClassPermissionInstruction -> lookup(
            perm.resolveParams(params).className,
            true
        )
    }
}