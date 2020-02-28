package de.robolab.model

data class Path(
        val source: Pair<Int, Int>,
        val sourceDirection: Direction,
        val target: Pair<Int, Int>,
        val targetDirection: Direction,
        val weight: Int,
        val exposure: Set<Pair<Int, Int>> = emptySet()
)
