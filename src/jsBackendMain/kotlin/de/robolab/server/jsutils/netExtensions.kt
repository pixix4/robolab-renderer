package de.robolab.server.jsutils

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.IHeader
import de.robolab.common.net.headers.LastModifiedHeader
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.common.utils.RobolabJson
import de.robolab.common.externaljs.http.ServerResponse
import de.robolab.server.externaljs.express.Response
import de.robolab.server.externaljs.express.format
import de.robolab.common.externaljs.toJSArray
import de.robolab.server.externaljs.express.status
import de.robolab.common.externaljs.dynamicOf
import de.robolab.server.model.asPlanetJsonInfo
import de.robolab.server.model.ServerPlanet
import kotlinx.serialization.builtins.ListSerializer

var ServerResponse.httpStatusCode: HttpStatusCode?
    get() = HttpStatusCode.get(this.statusCode)
    set(value) {
        this.statusCode = (value ?: HttpStatusCode.InternalServerError).code
    }

fun ServerResponse.setHeader(header: IHeader) = setHeader(header.name, header.value)

fun Response<*>.sendClientInfo(info: PlanetJsonInfo) = format(dynamicOf("json" to {
    send(RobolabJson.encodeToString(PlanetJsonInfo.serializer(), info))
}))

fun Response<*>.sendClientInfos(infos: List<PlanetJsonInfo>) = format(dynamicOf("json" to {
    send(RobolabJson.encodeToString(ListSerializer(PlanetJsonInfo.serializer()), infos))
}))

fun Response<*>.sendClientInfo(info: ServerPlanetInfo) = sendClientInfo(info.asPlanetJsonInfo())
fun Response<*>.sendClientInfos(infos: List<ServerPlanetInfo>) =
    sendClientInfos(infos.map(ServerPlanetInfo::asPlanetJsonInfo))

fun Response<*>.sendPlanet(planet: ServerPlanet) {
    setHeader(LastModifiedHeader(planet.lastModified))
    status(HttpStatusCode.Ok).format("json" to {
        send(planet.lines.toJSArray())
    }, "text" to {
        send(planet.lines.joinToString("\n"))
    })
}