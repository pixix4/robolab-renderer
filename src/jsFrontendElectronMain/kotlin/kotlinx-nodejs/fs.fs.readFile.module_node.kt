@file:JsQualifier("fs.readFile")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.readFile

import fs.`T$43`
import fs.`T$44`
import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, options: `T$43`): Promise<String>

external fun __promisify__(path: String, options: String): Promise<String>

external fun __promisify__(path: URL, options: `T$43`): Promise<String>

external fun __promisify__(path: URL, options: String): Promise<String>

external fun __promisify__(path: Number, options: `T$43`): Promise<String>

external fun __promisify__(path: Number, options: String): Promise<String>

external fun __promisify__(path: String, options: `T$44`? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: String, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: URL, options: `T$44`? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: URL, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: Number, options: `T$44`? = definedExternally): Promise<dynamic /* String | Buffer */>

external fun __promisify__(path: Number, options: String? = definedExternally): Promise<dynamic /* String | Buffer */>
