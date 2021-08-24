@file:JsQualifier("fs.truncate")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.truncate

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, len: Number? = definedExternally): Promise<Unit>

external fun __promisify__(path: URL, len: Number? = definedExternally): Promise<Unit>
