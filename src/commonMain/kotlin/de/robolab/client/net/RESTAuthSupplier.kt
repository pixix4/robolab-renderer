package de.robolab.client.net

import de.robolab.common.net.headers.Header

interface RESTAuthSupplier {
    val headers: List<Header>
}