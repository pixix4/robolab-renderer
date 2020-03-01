package de.robolab.model

data class Planet(
        val startPoint: Pair<Int, Int>,
        val isStartBlue: Boolean,
        val pathList: List<Path>,
        val targetList: List<Target> = emptyList(),
        val pathSelectList: List<PathSelect> = emptyList(),
        val animationTime: Double = 0.0
)
