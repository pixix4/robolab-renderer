@file:JsQualifier("fs.symlink")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.symlink

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(target: String, path: String, type: String? = definedExternally): Promise<Unit>

external fun __promisify__(target: String, path: URL, type: String? = definedExternally): Promise<Unit>

external fun __promisify__(target: URL, path: String, type: String? = definedExternally): Promise<Unit>

external fun __promisify__(target: URL, path: URL, type: String? = definedExternally): Promise<Unit>
