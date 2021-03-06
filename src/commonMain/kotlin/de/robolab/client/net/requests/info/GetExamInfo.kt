package de.robolab.client.net.requests.info


import de.robolab.client.net.*
import de.robolab.client.net.requests.IRESTRequest
import de.robolab.client.net.requests.IUnboundRESTRequest
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.client.net.requests.RESTResponse
import de.robolab.common.net.*
import de.robolab.common.utils.decode
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        val smallInfo: PlanetJsonInfo?
        val largeInfo: PlanetJsonInfo?

        init {
            serverResponse.requireOk(triggeringRequest)
            val json = serverResponse.requireJSONBody(triggeringRequest)
            isExam = json.jsonObject.getValue("isExam").jsonPrimitive.boolean
            if (isExam) {
                smallInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("smallPlanet"))
                largeInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("largePlanet"))
            } else {
                smallInfo = null
                largeInfo = null
            }
        }

        override fun toString(): String {
            return "ExamInfoResponse(isExam=$isExam, smallInfo=$smallInfo, largeInfo=$largeInfo)"
        }
    }
}

suspend fun IRobolabServer.getExamInfo() = request(GetExamInfo)
suspend fun IRobolabServer.getExamInfo(block: RequestBuilder.() -> Unit) = request(GetExamInfo, block)