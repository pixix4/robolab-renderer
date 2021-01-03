@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)
@file:JsModule("mqtt")
@file:JsNonModule

package de.robolab.client.communication.bindings

external fun connect(brokerUrl: String = definedExternally, opts: IClientOptions = definedExternally): MqttClient

external fun connect(brokerUrl: Any = definedExternally, opts: IClientOptions = definedExternally): MqttClient

external fun connect(): MqttClient
