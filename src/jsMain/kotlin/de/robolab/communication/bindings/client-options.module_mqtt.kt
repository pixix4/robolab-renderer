@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

package de.robolab.communication.bindings

import kotlin.js.Json

typealias StorePutCallback = () -> Unit

external interface `T$1` {
    var host: String
    var port: Number
    var protocol: String /* 'wss' | 'ws' | 'mqtt' | 'mqtts' | 'tcp' | 'ssl' | 'wx' | 'wxs' */
}

external interface `T$2` {
    var willDelayInterval: Number?
        get() = definedExternally
        set(value) = definedExternally
    var payloadFormatIndicator: Number?
        get() = definedExternally
        set(value) = definedExternally
    var messageExpiryInterval: Number?
        get() = definedExternally
        set(value) = definedExternally
    var contentType: String?
        get() = definedExternally
        set(value) = definedExternally
    var responseTopic: String?
        get() = definedExternally
        set(value) = definedExternally
    var correlationData: Any?
        get() = definedExternally
        set(value) = definedExternally
    var userProperties: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface `T$3` {
    var topic: String
    var payload: dynamic /* Buffer | String */
        get() = definedExternally
        set(value) = definedExternally
    var qos: Any
    var retain: Boolean
    var properties: `T$2`?
        get() = definedExternally
        set(value) = definedExternally
}

external interface `T$4` {
    var sessionExpiryInterval: Number?
        get() = definedExternally
        set(value) = definedExternally
    var receiveMaximum: Number?
        get() = definedExternally
        set(value) = definedExternally
    var maximumPacketSize: Number?
        get() = definedExternally
        set(value) = definedExternally
    var topicAliasMaximum: Number?
        get() = definedExternally
        set(value) = definedExternally
    var requestResponseInformation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var requestProblemInformation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var userProperties: Any?
        get() = definedExternally
        set(value) = definedExternally
    var authenticationMethod: String?
        get() = definedExternally
        set(value) = definedExternally
    var authenticationData: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IClientOptions : ISecureClientOptions {
    var port: Number?
        get() = definedExternally
        set(value) = definedExternally
    var host: String?
        get() = definedExternally
        set(value) = definedExternally
    var hostname: String?
        get() = definedExternally
        set(value) = definedExternally
    var path: String?
        get() = definedExternally
        set(value) = definedExternally
    var protocol: String?
        get() = definedExternally
        set(value) = definedExternally
    var wsOptions: Json?
        get() = definedExternally
        set(value) = definedExternally
    var keepalive: Number?
        get() = definedExternally
        set(value) = definedExternally
    var clientId: String?
        get() = definedExternally
        set(value) = definedExternally
    var protocolId: String?
        get() = definedExternally
        set(value) = definedExternally
    var protocolVersion: Number?
        get() = definedExternally
        set(value) = definedExternally
    var clean: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var reconnectPeriod: Number?
        get() = definedExternally
        set(value) = definedExternally
    var connectTimeout: Number?
        get() = definedExternally
        set(value) = definedExternally
    var username: String?
        get() = definedExternally
        set(value) = definedExternally
    var password: String?
        get() = definedExternally
        set(value) = definedExternally
    var incomingStore: Store?
        get() = definedExternally
        set(value) = definedExternally
    var outgoingStore: Store?
        get() = definedExternally
        set(value) = definedExternally
    var queueQoSZero: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var reschedulePings: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var servers: Array<`T$1`>?
        get() = definedExternally
        set(value) = definedExternally
    var resubscribe: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var will: `T$3`?
        get() = definedExternally
        set(value) = definedExternally
    var transformWsUrl: ((url: String, options: IClientOptions, client: MqttClient) -> String)?
        get() = definedExternally
        set(value) = definedExternally
    var properties: `T$4`?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ISecureClientOptions {
    var key: dynamic /* String | Array<String> | Buffer | Array<Buffer> | Array<Any> */
        get() = definedExternally
        set(value) = definedExternally
    var cert: dynamic /* String | Array<String> | Buffer | Array<Buffer> */
        get() = definedExternally
        set(value) = definedExternally
    var ca: dynamic /* String | Array<String> | Buffer | Array<Buffer> */
        get() = definedExternally
        set(value) = definedExternally
    var rejectUnauthorized: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IClientPublishOptions {
    var qos: Any?
        get() = definedExternally
        set(value) = definedExternally
    var retain: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var dup: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var cbStorePut: StorePutCallback?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IClientSubscribeOptions {
    var qos: Any
    var nl: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var rap: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var rh: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IClientReconnectOptions {
    var incomingStore: Store?
        get() = definedExternally
        set(value) = definedExternally
    var outgoingStore: Store?
        get() = definedExternally
        set(value) = definedExternally
}
