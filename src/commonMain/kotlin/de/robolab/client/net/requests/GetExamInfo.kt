package de.robolab.client.net.requests


import de.robolab.client.net.*
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.planet.ID
import de.robolab.common.utils.RobolabJson
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
    override val forceAuth: Boolean = false

    override fun parseResponse(serverResponse: ServerResponse) = ExamInfoResponse(serverResponse)

    class ExamInfoResponse(serverResponse: IServerResponse) : RESTResponse(serverResponse) {
        val isExam: Boolean
        val smallInfo: PlanetJsonInfo?
        val largeInfo: PlanetJsonInfo?

        init {
            if (status != HttpStatusCode.Ok) {
                isExam = false
                smallInfo = null
                largeInfo = null
            } else {
                val json = serverResponse.jsonBody!!
                isExam = json.jsonPrimitive.boolean
                if (isExam) {
                    smallInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("smallPlanet"))
                    largeInfo = PlanetJsonInfo.serializer().decode(json.jsonObject.getValue("largePlanet"))
                } else {
                    smallInfo = null
                    largeInfo = null
                }
            }
        }
    }
}

suspend fun IRobolabServer.getExamInfo(): GetExamInfo.ExamInfoResponse = request(GetExamInfo)
suspend fun IRobolabServer.getExamInfo(block: RequestBuilder.() -> Unit): GetExamInfo.ExamInfoResponse =
    request(GetExamInfo, block)