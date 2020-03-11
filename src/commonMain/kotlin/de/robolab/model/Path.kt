package de.robolab.model

data class Path(
        val source: Pair<Int, Int>,
        val sourceDirection: Direction,
        val target: Pair<Int, Int>,
        val targetDirection: Direction,
        val weight: Int,
        val exposure: Set<Pair<Int, Int>> = emptySet(),
        val controlPoints: List<Pair<Double, Double>> = emptyList()
) {

    fun equalPath(other: Path): Boolean {
        val thisP0 = source to sourceDirection
        val thisP1 = target to targetDirection

        val otherP0 = other.source to other.sourceDirection
        val otherP1 = other.target to other.targetDirection

        return thisP0 == otherP0 && thisP1 == otherP1 || thisP0 == otherP1 && thisP1 == otherP0
    }
}
