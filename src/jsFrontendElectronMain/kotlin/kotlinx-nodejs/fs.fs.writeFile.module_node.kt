@file:JsQualifier("fs.writeFile")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.writeFile

import fs.`T$45`
import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, data: Any, options: `T$45`? = definedExternally): Promise<Unit>

external fun __promisify__(path: String, data: Any, options: String? = definedExternally): Promise<Unit>

external fun __promisify__(path: URL, data: Any, options: `T$45`? = definedExternally): Promise<Unit>

external fun __promisify__(path: URL, data: Any, options: String? = definedExternally): Promise<Unit>

external fun __promisify__(path: Number, data: Any, options: `T$45`? = definedExternally): Promise<Unit>

external fun __promisify__(path: Number, data: Any, options: String? = definedExternally): Promise<Unit>
