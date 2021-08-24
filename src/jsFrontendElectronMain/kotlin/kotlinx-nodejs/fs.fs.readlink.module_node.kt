@file:JsQualifier("fs.readlink")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.readlink

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Promise<String>

external fun __promisify__(path: URL, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Promise<String>

external fun __promisify__(path: String, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: URL, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>
