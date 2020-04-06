package de.robolab.model

data class Planet(
        val name: String,
        val startPoint: StartPoint?,
        val bluePoint: Coordinate?,
        val pathList: List<Path>,
        val targetList: List<TargetPoint>,
        val pathSelectList: List<PathSelect>
) {
    companion object {
        val EMPTY = Planet(
                "",
                null,
                null,
                emptyList(),
                emptyList(),
                emptyList()
        )
    }
}
