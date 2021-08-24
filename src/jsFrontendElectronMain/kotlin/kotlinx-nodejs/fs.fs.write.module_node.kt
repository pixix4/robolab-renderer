@file:JsQualifier("fs.write")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.write

import kotlin.js.Promise

external fun <TBuffer> __promisify__(fd: Number, buffer: TBuffer = definedExternally, offset: Number = definedExternally, length: Number = definedExternally, position: Number? = definedExternally): Promise<`T$39`<TBuffer>>

external fun __promisify__(fd: Number, string: Any, position: Number? = definedExternally, encoding: String? = definedExternally): Promise<`T$40`>
