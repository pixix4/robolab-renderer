@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION")

package de.robolab.communication.bindings

external interface IStoreOptions {
    var clean: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}
