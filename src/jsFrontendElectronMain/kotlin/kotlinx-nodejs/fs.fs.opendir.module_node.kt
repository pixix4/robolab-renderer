@file:JsQualifier("fs.opendir")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.opendir

import fs.Dir
import fs.OpenDirOptions
import kotlin.js.Promise

external fun __promisify__(path: String, options: OpenDirOptions = definedExternally): Promise<Dir>
