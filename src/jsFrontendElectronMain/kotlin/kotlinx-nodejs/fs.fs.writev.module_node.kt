@file:JsQualifier("fs.writev")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.writev

import fs.WriteVResult
import kotlin.js.Promise

external fun __promisify__(fd: Number, buffers: Array<dynamic /* Uint8Array | Uint8ClampedArray | Uint16Array | Uint32Array | Int8Array | Int16Array | Int32Array | Float32Array | Float64Array | DataView */>, position: Number = definedExternally): Promise<WriteVResult>
