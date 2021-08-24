@file:JsQualifier("fs.chown")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.chown

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, uid: Number, gid: Number): Promise<Unit>

external fun __promisify__(path: URL, uid: Number, gid: Number): Promise<Unit>
