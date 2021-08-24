@file:JsQualifier("fs.fchown")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.fchown

import kotlin.js.Promise

external fun __promisify__(fd: Number, uid: Number, gid: Number): Promise<Unit>
