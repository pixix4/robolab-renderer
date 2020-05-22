package de.robolab.common.utils


fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").toLowerCase()
