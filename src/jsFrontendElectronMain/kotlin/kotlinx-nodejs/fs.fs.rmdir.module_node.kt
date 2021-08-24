@file:JsQualifier("fs.rmdir")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.rmdir

import fs.RmDirAsyncOptions
import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String, options: RmDirAsyncOptions = definedExternally): Promise<Unit>

external fun __promisify__(path: URL, options: RmDirAsyncOptions = definedExternally): Promise<Unit>
