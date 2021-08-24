@file:JsQualifier("fs.fdatasync")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.fdatasync

import kotlin.js.Promise

external fun __promisify__(fd: Number): Promise<Unit>
