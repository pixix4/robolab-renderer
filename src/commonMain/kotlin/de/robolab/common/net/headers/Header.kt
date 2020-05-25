package de.robolab.common.net.headers

interface IHeader{
    val name: String
    val value: String
}

abstract class Header(
    override val name:String,
    override val value:String
): IHeader