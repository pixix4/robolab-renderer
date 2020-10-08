package de.robolab.common.net.headers

interface IHeader {
    val name: String
    val value: String
    operator fun component1() = name
    operator fun component2() = value
}

abstract class Header(
    override val name: String,
    override val value: String
) : IHeader

fun mapOf(vararg headers: IHeader): Map<String, List<String>> =
    headers.groupBy(IHeader::name, IHeader::value).toLowerCaseKeys()