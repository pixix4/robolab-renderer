@file:JsQualifier("fs.link")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.link

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(existingPath: String, newPath: String): Promise<Unit>

external fun __promisify__(existingPath: String, newPath: URL): Promise<Unit>

external fun __promisify__(existingPath: URL, newPath: String): Promise<Unit>

external fun __promisify__(existingPath: URL, newPath: URL): Promise<Unit>
