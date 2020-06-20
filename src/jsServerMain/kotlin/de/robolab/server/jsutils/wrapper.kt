@file:Suppress("NOTHING_TO_INLINE")

package de.robolab.server.jsutils

import de.robolab.server.externaljs.*

typealias JSBiCallback<E, T> = (err: E, res: T) -> Unit
typealias JSErrorCallback<E> = (err: E) -> Unit
typealias JSDynErrorCallback = JSErrorCallback<dynamic>
typealias JSBiDynErrorCallback<T> = JSBiCallback<dynamic, T>
typealias JSDynBiCallback = JSBiDynErrorCallback<dynamic>

inline fun Any?.hasJSValue() = (this != null) && (this != undefined)

inline fun Any?.isUndefined() = this == undefined

inline fun Any?.isNull() = this == null

inline fun Any?.truthy() = !this.falsy()

@Suppress("DUPLICATE_LABEL_IN_WHEN")
fun Any?.falsy() = when (this) {
    false -> true
    0 -> true
    -0 -> true
    "" -> true
    null -> true
    undefined -> true
    is Double -> this.isNaN()
    is Float -> this.isNaN()
    else -> false
}

private val jsTruthyTest: (Any?) -> Boolean = js(
    """function jsTruthy(val){return !!val}"""
) as ((Any?) -> Boolean)

fun Any?.jsFalsy() = !jsTruthyTest(this)
fun Any?.jsTruthy() = jsTruthyTest(this)
fun jsFalsy(obj: Any?) = !jsTruthyTest(obj)
fun jsTruthy(obj: Any?) = jsTruthyTest(obj)
fun jsFalsy(obj: dynamic) = !jsTruthyTest(obj as Any?)
fun jsTruthy(obj: dynamic) = jsTruthyTest(obj as Any?)

private val jsAssignCall: (dynamic, dynamic) -> dynamic = js("Object.assign") as (dynamic, dynamic) -> dynamic

fun jsAssign(target: dynamic, source: dynamic): dynamic = jsAssignCall(target, source)

fun jsAssign(target: dynamic, vararg sources: dynamic): dynamic {
    return arrayOf(target, *sources).reduce(jsAssignCall)
}

private val jsCreateCall: (dynamic) -> dynamic = js("Object.create") as (dynamic) -> dynamic

fun jsCreate(template: dynamic): dynamic = jsCreateCall(template)
fun <T> jsCreate(template: T): T = jsCreateCall(template).unsafeCast<T>()
fun <T> T.jsCreate(): T = jsCreateCall(this).unsafeCast<T>()

fun <T> T.jsClone(): T = jsAssign(emptyDynamic(), this).unsafeCast<T>()
fun <T> jsClone(target: T): T = jsAssign(emptyDynamic(), target).unsafeCast<T>()

private val jsCreateDelegateCallFactory: () -> (dynamic, JSArray<String>) -> dynamic =
    js(
        "function jsCreateDelegateCallFactory() {\n" +
                "    var defaultExceptions = []//[\"constructor\",\"__defineGetter__\",\"__defineSetter__\",\"hasOwnProperty\"]\n" +
                "\n" +
                "    function delegateProp(original, copy, propName){\n" +
                "        if (propName in copy){\n" +
                "            return\n" +
                "        }\n" +
                "        Object.defineProperty(copy, propName, {\n" +
                "            get: function() {\n" +
                "                var val= original[propName]\n" +
                "                if (typeof val == \"function\")\n" +
                "                    return val.bind(original)\n" +
                "                return val\n" +
                "            },\n" +
                "            set: function(v) {\n" +
                "                original[propName] = v\n" +
                "            }\n" +
                "        })\n" +
                "    }\n" +
                "\n" +
                "    function applyDelegates(original, copy, currentProto, except) {\n" +
                "        var protoProto = Object.getPrototypeOf(currentProto)\n" +
                "        if (protoProto){\n" +
                "            applyDelegates(original, copy, protoProto, except)\n" +
                "        }\n" +
                "        var propDescriptors = Object.getOwnPropertyDescriptors(currentProto)\n" +
                "        var x\n" +
                "        for (x in propDescriptors){\n" +
                "            if (defaultExceptions.includes(x) || (except && (except.includes(x)))){\n" +
                "                continue\n" +
                "            }\n" +
                "            delegateProp(original, copy, x)\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    return function jsCreateDelegateCall(obj, exceptArray) {\n" +
                "        var copy = Object.create(null)\n" +
                "        applyDelegates(obj, copy, obj, exceptArray)\n" +
                "        return copy\n" +
                "    }\n" +
                "}\n" +
                "\n"
    ) as () -> (dynamic, JSArray<String>) -> dynamic
private val jsCreateDelegateCall: (dynamic, JSArray<String>) -> dynamic = jsCreateDelegateCallFactory()

fun <T> jsCreateDelegate(target: T, vararg except: String): T =
    jsCreateDelegateCall(target, jsArrayOf(*except)).unsafeCast<T>()

fun <T> T.jsCreateDelegate(vararg except: String): T = jsCreateDelegateCall(this, jsArrayOf(*except)).unsafeCast<T>()