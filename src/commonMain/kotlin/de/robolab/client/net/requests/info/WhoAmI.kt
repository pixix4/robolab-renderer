package de.robolab.client.net.requests.info


import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.JsonRestResponse
import de.robolab.common.auth.User
import de.robolab.common.net.*

object WhoAmI : IUnboundRESTRequest<WhoAmI.Response> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/info/whoami"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf("Accept" to listOf(MIMEType.JSON.primaryName))

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, WhoAmI::Response)

    class Response(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<Response>) :
        JsonRestResponse<User>(serverResponse, triggeringRequest, User.serializer()) {

        val user: User = decodedValue

        private val sub: String = user.internalName
        private val accessLevel: Int = user.accessLevel.numericLevel
        private val anonymous: Boolean = user.userID == User.Anonymous.userID

        override fun toString(): String {
            return "Response(sub='$sub', accessLevel='$accessLevel', anonymous=$anonymous)"
        }
    }
}

suspend fun IRobolabServer.whoami() = request(WhoAmI)
suspend fun IRobolabServer.whoami(block: RequestBuilder.() -> Unit) = request(WhoAmI, block)