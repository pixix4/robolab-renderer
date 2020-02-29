package de.robolab.model

data class Target(
        val target: Pair<Int, Int>,
        val exposure: Set<Pair<Int, Int>> = emptySet()
)
