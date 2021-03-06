package de.robolab.common.testing

sealed class TestSignal {
    data class Ordered(val order: Int) : TestSignal(), Comparable<Ordered> {
        override fun compareTo(other: Ordered): Int {
            return order.compareTo(other.order)
        }

        override fun toString(): String = "O:$order"
    }

    data class Unordered(val label: String) : TestSignal() {
        override fun toString() = "U:\"$label\""
    }
}

fun TestSignal?.serialize(): String {
    return when (this) {
        null -> ""
        is TestSignal.Ordered -> "($order)"
        is TestSignal.Unordered -> "($label)"
    }
}