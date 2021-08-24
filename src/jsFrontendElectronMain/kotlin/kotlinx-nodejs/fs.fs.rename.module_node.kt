@file:JsQualifier("fs.rename")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.rename

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(oldPath: String, newPath: String): Promise<Unit>

external fun __promisify__(oldPath: String, newPath: URL): Promise<Unit>

external fun __promisify__(oldPath: URL, newPath: String): Promise<Unit>

external fun __promisify__(oldPath: URL, newPath: URL): Promise<Unit>
