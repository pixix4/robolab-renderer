@file:JsQualifier("fs.readdir")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.readdir

import fs.Dirent
import fs.`T$38`
import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, options: dynamic /* `T$35`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Promise<Array<String>>

external fun __promisify__(path: URL, options: dynamic /* `T$35`? | "ascii" | "utf8" | "utf-8" | "utf16le" | "ucs2" | "ucs-2" | "base64" | "latin1" | "binary" | "hex" */ = definedExternally): Promise<Array<String>>

external fun __promisify__(path: String, options: `T$38`): Promise<Array<Dirent>>

external fun __promisify__(path: URL, options: `T$38`): Promise<Array<Dirent>>
