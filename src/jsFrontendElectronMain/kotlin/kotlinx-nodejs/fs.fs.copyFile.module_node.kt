@file:JsQualifier("fs.copyFile")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.copyFile

import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(src: String, dst: String, flags: Number = definedExternally): Promise<Unit>

external fun __promisify__(src: String, dst: URL, flags: Number = definedExternally): Promise<Unit>

external fun __promisify__(src: URL, dst: String, flags: Number = definedExternally): Promise<Unit>

external fun __promisify__(src: URL, dst: URL, flags: Number = definedExternally): Promise<Unit>
