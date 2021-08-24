@file:JsQualifier("fs.mkdtemp")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.mkdtemp

import fs.`T$32`
import kotlin.js.Promise

external fun __promisify__(prefix: String, options: `T$32`? = definedExternally): Promise<String>

external fun __promisify__(prefix: String, options: String /* "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" | "buffer" */ = definedExternally): dynamic /* Promise | Promise */

external fun __promisify__(prefix: String, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>
