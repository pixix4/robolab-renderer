package de.robolab.common.planet

sealed class TestSignal {
    data class Ordered(val order: Int): TestSignal()
    data class Unordered(val label: String): TestSignal()
}

fun TestSignal?.serialize(): String {
    return when (this) {
        null -> ""
        is TestSignal.Ordered -> "($order)"
        is TestSignal.Unordered -> "($label)"
    }
}