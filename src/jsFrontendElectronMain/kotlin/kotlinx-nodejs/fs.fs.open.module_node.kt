@file:JsQualifier("fs.open")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.open

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, flags: String, mode: String? = definedExternally): Promise<Number>

external fun __promisify__(path: String, flags: String, mode: Number? = definedExternally): Promise<Number>

external fun __promisify__(path: String, flags: Number, mode: String? = definedExternally): Promise<Number>

external fun __promisify__(path: String, flags: Number, mode: Number? = definedExternally): Promise<Number>

external fun __promisify__(path: URL, flags: String, mode: String? = definedExternally): Promise<Number>

external fun __promisify__(path: URL, flags: String, mode: Number? = definedExternally): Promise<Number>

external fun __promisify__(path: URL, flags: Number, mode: String? = definedExternally): Promise<Number>

external fun __promisify__(path: URL, flags: Number, mode: Number? = definedExternally): Promise<Number>
