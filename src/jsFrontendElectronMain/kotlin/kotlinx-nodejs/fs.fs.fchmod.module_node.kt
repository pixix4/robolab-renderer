@file:JsQualifier("fs.fchmod")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.fchmod

import kotlin.js.Promise

external fun __promisify__(fd: Number, mode: String): Promise<Unit>

external fun __promisify__(fd: Number, mode: Number): Promise<Unit>
