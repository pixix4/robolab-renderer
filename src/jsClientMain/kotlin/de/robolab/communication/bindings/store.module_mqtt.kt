@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

package de.robolab.communication.bindings

external open class Store(options: IStoreOptions) {
    open fun put(packet: Any, cb: Function<*> = definedExternally): Store /* this */
    open fun createStream(): Any
    open fun del(packet: Any, cb: Function<*>): Store /* this */
    open fun get(packet: Any, cb: Function<*>): Store /* this */
    open fun close(cb: Function<*>)
}
