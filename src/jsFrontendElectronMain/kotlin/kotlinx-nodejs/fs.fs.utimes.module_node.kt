@file:JsQualifier("fs.utimes")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.utimes

import org.w3c.dom.url.URL
import kotlin.js.Date
import kotlin.js.Promise

external fun __promisify__(path: String, atime: String, mtime: dynamic /* String | Number | Date */): Promise<Unit>

external fun __promisify__(path: String, atime: Number, mtime: dynamic /* String | Number | Date */): Promise<Unit>

external fun __promisify__(path: String, atime: Date, mtime: dynamic /* String | Number | Date */): Promise<Unit>

external fun __promisify__(path: URL, atime: String, mtime: dynamic /* String | Number | Date */): Promise<Unit>

external fun __promisify__(path: URL, atime: Number, mtime: dynamic /* String | Number | Date */): Promise<Unit>

external fun __promisify__(path: URL, atime: Date, mtime: dynamic /* String | Number | Date */): Promise<Unit>
