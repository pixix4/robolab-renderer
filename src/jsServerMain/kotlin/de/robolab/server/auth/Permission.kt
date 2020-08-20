package de.robolab.server.auth

class Permission(val type: PermissionType, val parameters: List<String>) {

    constructor(name: String, parameters: List<String>) : this(
        PermissionType.fromName(
            name
        )!!, parameters)

    enum class PermissionType(val parameterCount: Int = 0) {
        ReadPublicPlanets,
        ReadPublicInfo,
        ReadMessages(1),
        WriteMessages(1),
        ReadExamInfo,
        ReadExamPlanets,
        ReadPrivatePlanets,
        WritePrivatePlanets,
        WritePublicPlanets,
        WriteExamInfo
        ;

        companion object {
            private val map: Map<String, PermissionType> = values().associateBy { it.name.toLowerCase() }
            fun fromName(name: String): PermissionType? = map[name.toLowerCase()]
        }
    }
}