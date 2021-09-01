package de.robolab.client.net.requests.info

import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.client.net.requests.RESTResponse
import de.robolab.common.net.*
import de.robolab.common.utils.decode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object GetExamInfo : IUnboundRESTRequest<GetExamInfo.ExamInfoResponse> {
    override val requestMethod: HttpMethod = HttpMethod.GET
    override val requestPath: String = "/api/info/exam"
    override val requestBody: String? = null
    override val requestQuery: Map<String, String> = emptyMap()
    override val requestHeader: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) =
        parseResponseCatchingWrapper(serverResponse, this, GetExamInfo::ExamInfoResponse)

    class ExamInfoResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<ExamInfoResponse>) :
        RESTResponse(serverResponse) {
        val isExam: Boolean
        val planets: List<PlanetExamPlanet>

        init {
            serverResponse.requireOk(triggeringRequest)
            val json = serverResponse.requireJSONBody(triggeringRequest)
            isExam = json.jsonObject.getValue("isExam").jsonPrimitive.boolean
            if (isExam) {
                planets = decode(json.jsonObject.getValue("planets"))
            } else {
                planets = emptyList()
            }
        }

        override fun toString(): String {
            return "ExamInfoResponse(isExam=$isExam, planets=$planets)"
        }
    }
}

@Serializable
data class PlanetExamPlanet(
    val name: String,
    val info: PlanetJsonInfo
)

suspend fun IRobolabServer.getExamInfo() = request(GetExamInfo)
suspend fun IRobolabServer.getExamInfo(block: RequestBuilder.() -> Unit) = request(GetExamInfo, block)
