@file:JsQualifier("fs.realpathSync")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.realpathSync

import org.w3c.dom.url.URL

external fun native(path: String, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun native(path: URL, options: dynamic /* `T$32`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): String

external fun native(path: String, options: String? = definedExternally): dynamic /* String | Buffer */

external fun native(path: URL, options: String? = definedExternally): dynamic /* String | Buffer */
