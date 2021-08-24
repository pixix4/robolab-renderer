@file:JsQualifier("fs.stat")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE")
package fs.stat

import fs.Stats
import org.w3c.dom.url.URL
import kotlin.js.Promise

external fun __promisify__(path: String): Promise<Stats>

external fun __promisify__(path: URL): Promise<Stats>
