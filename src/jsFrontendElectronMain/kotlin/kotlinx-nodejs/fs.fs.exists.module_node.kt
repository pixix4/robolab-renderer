@file:JsQualifier("fs.exists")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.exists

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String): Promise<Boolean>

external fun __promisify__(path: URL): Promise<Boolean>
