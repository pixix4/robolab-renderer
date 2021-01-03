package de.robolab.common.externaljs.path

private val module: dynamic = js("require(\"path\")")

private fun joinPath2(path1: String, path2: String): String = module.join(path1, path2).unsafeCast<String>()

fun joinPath(vararg paths: String): String = paths.reduceRight(::joinPath2)

fun normalizePath(path: String): String = module.normalize(path).unsafeCast<String>()

//from https://security.stackexchange.com/a/123723
@Suppress("RegExpRedundantEscape")
private val safeSuffixRegex: Regex = """^(\.\.(\/|\\|$))+""".toRegex()
private fun safeJoinPath2(path1: String, path2: String): String {
    val safeSuffix = normalizePath(path2).replace(safeSuffixRegex, "")
    return joinPath(path1, safeSuffix)
}

fun safeJoinPath(rootPath: String, vararg paths: String, strictNesting: Boolean = true): String {
    return when {
        paths.isEmpty() -> rootPath
        strictNesting -> (listOf(rootPath) + paths).reduceRight(::safeJoinPath2)
        else -> safeJoinPath2(rootPath, joinPath(*paths))
    }
}