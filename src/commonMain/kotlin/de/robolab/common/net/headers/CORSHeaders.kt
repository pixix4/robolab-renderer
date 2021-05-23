package de.robolab.common.net.headers

import de.robolab.common.net.HttpMethod


class AccessControlAllowMethods : Header {

    val methods: List<HttpMethod?>
    val wildcard: Boolean

    constructor(methods: List<HttpMethod>) : super(
        AccessControlAllowMethods.name,
        methods.joinToString { it.name.uppercase() }) {
        this.methods = methods
        this.wildcard = false
    }

    constructor(vararg methods: HttpMethod) : this(methods.toList())

    constructor(value: String) : super(AccessControlAllowMethods.name, value) {
        if(value == "*"){
            wildcard = true
            methods = HttpMethod.values().toList()
        }else{
            wildcard = false
            methods = value.split(""", ?""".toRegex()).map {
                HttpMethod.parse(it) ?: throw IllegalArgumentException("Could not parse HttpMethod '$it' from '$value'")
            }
        }

    }

    companion object {
        const val name: String = "access-control-allow-methods"
        val All: AccessControlAllowMethods = AccessControlAllowMethods("*")
    }
}