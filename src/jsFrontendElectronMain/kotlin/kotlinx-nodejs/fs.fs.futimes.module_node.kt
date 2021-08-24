@file:JsQualifier("fs.futimes")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.futimes

import kotlin.js.Date
import kotlin.js.Promise

external fun __promisify__(fd: Number, atime: String, mtime: String): Promise<Unit>

external fun __promisify__(fd: Number, atime: String, mtime: Number): Promise<Unit>

external fun __promisify__(fd: Number, atime: String, mtime: Date): Promise<Unit>

external fun __promisify__(fd: Number, atime: Number, mtime: String): Promise<Unit>

external fun __promisify__(fd: Number, atime: Number, mtime: Number): Promise<Unit>

external fun __promisify__(fd: Number, atime: Number, mtime: Date): Promise<Unit>

external fun __promisify__(fd: Number, atime: Date, mtime: String): Promise<Unit>

external fun __promisify__(fd: Number, atime: Date, mtime: Number): Promise<Unit>

external fun __promisify__(fd: Number, atime: Date, mtime: Date): Promise<Unit>
