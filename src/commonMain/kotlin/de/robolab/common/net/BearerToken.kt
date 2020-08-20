package de.robolab.common.net

import de.robolab.client.net.RESTAuthSupplier
import de.robolab.common.net.headers.Header

open class BearerToken(
    val rawToken: String
) : RESTAuthSupplier{
    override val headers: List<Header> = listOf()
}