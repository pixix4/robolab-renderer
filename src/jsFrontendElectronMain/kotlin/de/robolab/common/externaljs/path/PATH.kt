package de.robolab.common.externaljs.path

private val module = js("require('path')")

external class PathObject {
    val dir: String
    val root: String
    val base: String
    val name: String
    val ext: String
}

//from https://security.stackexchange.com/a/123723
@Suppress("RegExpRedundantEscape")
private val safeSuffixRegex: Regex = """^(\.\.(\/|\\|$))+""".toRegex()
private fun safeJoinPath2(path1: String, path2: String): String {
    val safeSuffix = module.normalize(path2).replace(safeSuffixRegex, "")
    return module.join(path1, safeSuffix)
}

fun safeJoinPath(rootPath: String, vararg paths: String, strictNesting: Boolean = true): String {
    return when {
        paths.isEmpty() -> rootPath
        else -> (listOf(rootPath) + paths).reduceRight(::safeJoinPath2)
    }
}

fun pathJoin(rootPath: String, vararg paths: String, strictNesting: Boolean = true): String {
    return safeJoinPath(rootPath, *paths, strictNesting = strictNesting)
}

fun pathResolve(p: String): String {
    return module.resolve(p)
}

fun pathNormalize(p: String): String {
    return module.normalize(p)
}

fun pathParse(p: String): PathObject {
    return module.parse(p)
}

fun pathRelative(p: String, o: String): String {
    return module.relative(p, o)
}
