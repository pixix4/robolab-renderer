@file:JsQualifier("fs.read")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.read

import kotlin.js.Promise

external fun <TBuffer> __promisify__(fd: Number, buffer: TBuffer, offset: Number, length: Number, position: Number?): Promise<`T$41`<TBuffer>>
