package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.*
import de.robolab.common.utils.decode
import kotlinx.serialization.Serializable

object WhoAmI : IUnboundRESTRequest<WhoAmI.Response> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/info/whoami"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, ::Response)

    class Response(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<Response>) :
        RESTResponse(serverResponse) {

        val sub: String
        val accessLevel: Int
        val accessLevelName: String
        val anonymous: Boolean

        init {
            if (status != HttpStatusCode.Ok) {
                `throw`(triggeringRequest)
            } else {
                val json = serverResponse.jsonBody!!
                val obj = WhoAmIObject.serializer().decode(json)

                sub = obj.sub
                accessLevel = obj.accessLevel
                anonymous = obj.anonymous

                accessLevelName = when {
                    accessLevel >= 40 -> "Admin"
                    accessLevel >= 10 -> "Tutor"
                    accessLevel >= 0 -> "Student"
                    else -> "None"
                }
            }
        }

        override fun toString(): String {
            return "Response(sub='$sub', accessLevel='$accessLevel', anonymous=$anonymous)"
        }
    }

    @Suppress("unused")
    @Serializable
    class WhoAmIObject(
        val sub: String,
        val accessLevel: Int,
        val anonymous: Boolean,
    )
}

suspend fun IRobolabServer.whoami() = request(WhoAmI)
suspend fun IRobolabServer.whoami(block: RequestBuilder.() -> Unit) = request(WhoAmI, block)