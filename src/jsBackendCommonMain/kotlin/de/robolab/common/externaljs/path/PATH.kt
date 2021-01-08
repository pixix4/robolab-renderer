package de.robolab.common.externaljs.path

import path.path

//from https://security.stackexchange.com/a/123723
@Suppress("RegExpRedundantEscape")
private val safeSuffixRegex: Regex = """^(\.\.(\/|\\|$))+""".toRegex()
private fun safeJoinPath2(path1: String, path2: String): String {
    val safeSuffix = path.normalize(path2).replace(safeSuffixRegex, "")
    return path.join(path1, safeSuffix)
}

fun safeJoinPath(rootPath: String, vararg paths: String, strictNesting: Boolean = true): String {
    return when {
        paths.isEmpty() -> rootPath
        strictNesting -> (listOf(rootPath) + paths).reduceRight(::safeJoinPath2)
        else -> safeJoinPath2(rootPath, path.join(*paths))
    }
}
