package de.robolab.server.jsutils

import de.robolab.server.externaljs.http.ServerResponse
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.IHeader
import de.robolab.common.net.headers.LastModifiedHeader
import de.robolab.common.planet.ClientPlanetInfo
import de.robolab.common.planet.ServerPlanetInfo
import de.robolab.server.externaljs.express.Response
import de.robolab.server.externaljs.express.format
import de.robolab.server.externaljs.express.formatReceiving
import de.robolab.server.externaljs.toJSArray
import de.robolab.server.model.asClientPlanetInfo
import de.robolab.server.model.toIDString
import de.robolab.server.data.json
import de.robolab.server.externaljs.express.status
import de.robolab.server.model.ServerPlanet
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.list

var ServerResponse.httpStatusCode: HttpStatusCode?
    get() = HttpStatusCode.get(this.statusCode)
    set(value) {
        this.statusCode = (value ?: HttpStatusCode.InternalServerError).code
    }

fun ServerResponse.setHeader(header: IHeader) = setHeader(header.name, header.value)

fun Response<*>.sendClientInfo(info: ClientPlanetInfo) = format("json" to {
    send(json.encodeToString(ClientPlanetInfo.serializer(), info))
}, "text" to {
    send(info.toPlaintextString())
})

fun Response<*>.sendClientInfos(infos: List<ClientPlanetInfo>) = format("json" to {
    send(json.encodeToString(ListSerializer(ClientPlanetInfo.serializer()), infos))
}, "text" to {
    send(infos.joinToString("\n", transform = ClientPlanetInfo::toPlaintextString))
})

fun Response<*>.sendClientInfo(info: ServerPlanetInfo) = sendClientInfo(info.asClientPlanetInfo())
fun Response<*>.sendClientInfos(infos: List<ServerPlanetInfo>) =
    sendClientInfos(infos.map(ServerPlanetInfo::asClientPlanetInfo))

fun Response<*>.sendPlanet(planet: ServerPlanet) {
    setHeader(LastModifiedHeader(planet.lastModified))
    status(HttpStatusCode.Ok).format("json" to {
        send(planet.lines.toJSArray())
    }, "text" to {
        send(planet.lines.joinToString("\n"))
    })
}