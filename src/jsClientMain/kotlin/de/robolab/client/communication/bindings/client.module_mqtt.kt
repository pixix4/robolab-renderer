@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

package de.robolab.client.communication.bindings

external interface ISubscriptionGrant {
    var topic: String
    var qos: dynamic /* QoS | Number */
        get() = definedExternally
        set(value) = definedExternally
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

external interface ISubscriptionRequest {
    var topic: String
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

external interface `T$0` {
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

external interface ISubscriptionMap {
    @nativeGetter
    operator fun get(topic: String): `T$0`?

    @nativeSetter
    operator fun set(topic: String, value: `T$0`)
}

typealias ClientSubscribeCallback = (err: Error, granted: Array<ISubscriptionGrant>) -> Unit

typealias OnMessageCallback = (topic: String, payload: dynamic, packet: dynamic) -> Unit

typealias OnPacketCallback = (packet: dynamic) -> Unit

typealias OnErrorCallback = (error: Error) -> Unit

typealias PacketCallback = (error: Error, packet: dynamic) -> Any

typealias CloseCallback = () -> Unit

open external class MqttClient(streamBuilder: dynamic, options: IClientOptions) {
    open var connected: Boolean
    open var disconnecting: Boolean
    open var disconnected: Boolean
    open var reconnecting: Boolean
    open var incomingStore: Store
    open var outgoingStore: Store
    open var options: IClientOptions
    open var queueQoSZero: Boolean
    open fun on(event: String, cb: dynamic): MqttClient /* this */
    open fun once(event: String /* 'message' */, cb: OnMessageCallback): MqttClient /* this */
    open fun once(event: String, cb: OnPacketCallback): MqttClient /* this */
    open fun once(event: String /* 'error' */, cb: OnErrorCallback): MqttClient /* this */
    open fun once(event: String, cb: Function<*>): MqttClient /* this */
    open fun publish(topic: String, message: String, opts: IClientPublishOptions, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun publish(topic: String, message: dynamic, opts: IClientPublishOptions, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun publish(topic: String, message: String, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun publish(topic: String, message: dynamic, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun subscribe(topic: String, opts: IClientSubscribeOptions, callback: ClientSubscribeCallback = definedExternally): MqttClient /* this */
    open fun subscribe(topic: Array<String>, opts: IClientSubscribeOptions, callback: ClientSubscribeCallback = definedExternally): MqttClient /* this */
    open fun subscribe(topic: String, callback: ClientSubscribeCallback = definedExternally): MqttClient /* this */
    open fun subscribe(topic: Array<String>, callback: ClientSubscribeCallback = definedExternally): MqttClient /* this */
    open fun subscribe(topic: ISubscriptionMap, callback: ClientSubscribeCallback = definedExternally): MqttClient /* this */
    open fun unsubscribe(topic: String, opts: Any = definedExternally, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun unsubscribe(topic: Array<String>, opts: Any = definedExternally, callback: PacketCallback = definedExternally): MqttClient /* this */
    open fun end(force: Boolean = definedExternally, opts: Any = definedExternally, cb: CloseCallback = definedExternally): MqttClient /* this */
    open fun removeOutgoingMessage(mid: Number): MqttClient /* this */
    open fun reconnect(opts: IClientReconnectOptions = definedExternally): MqttClient /* this */
    open fun handleMessage(packet: dynamic, callback: PacketCallback)
    open fun getLastMessageId(): Number
}
