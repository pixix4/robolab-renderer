package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.`throw`
import de.robolab.common.net.parseResponseCatchingWrapper
import de.robolab.common.utils.decode
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object GetExamInfo : IRESTRequest<GetExamInfo.ExamInfoResponse> {
    override val method: HttpMethod = HttpMethod.GET
    override val path: String = "/api/info/exam"
    override val body: String? = null
    override val query: Map<String, String> = emptyMap()
    override val headers: Map<String, List<String>> = mapOf()

    override fun parseResponse(serverResponse: ServerResponse) = parseResponseCatchingWrapper(serverResponse,this,::ExamInfoResponse)

    class ExamInfoResponse(serverResponse: IServerResponse, triggeringRequest: IRESTRequest<ExamInfoResponse>) : RESTResponse(serverResponse) {
        val isExam: Boolean
        val smallInfo: PlanetJsonInfo?
        val largeInfo: PlanetJsonInfo?

        init {
            if (status != HttpStatusCode.Ok) {
                `throw`(triggeringRequest)
            } else {
                val json = serverResponse.jsonBody!!
                isExam = json.jsonObject.getValue("isExam").jsonPrimitive.boolean
                if (isExam) {
                    smallInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("smallPlanet"))
                    largeInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("largePlanet"))
                } else {
                    smallInfo = null
                    largeInfo = null
                }
            }
        }

        override fun toString(): String {
            return "ExamInfoResponse(isExam=$isExam, smallInfo=$smallInfo, largeInfo=$largeInfo)"
        }
    }
}

suspend fun IRobolabServer.getExamInfo() = request(GetExamInfo)
suspend fun IRobolabServer.getExamInfo(block: RequestBuilder.() -> Unit)= request(GetExamInfo, block)