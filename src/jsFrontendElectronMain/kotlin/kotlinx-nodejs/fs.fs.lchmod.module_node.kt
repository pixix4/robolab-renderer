@file:JsQualifier("fs.lchmod")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.lchmod

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, mode: String): Promise<Unit>

external fun __promisify__(path: String, mode: Number): Promise<Unit>

external fun __promisify__(path: URL, mode: String): Promise<Unit>

external fun __promisify__(path: URL, mode: Number): Promise<Unit>
