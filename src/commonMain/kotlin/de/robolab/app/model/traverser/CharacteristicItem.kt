package de.robolab.app.model.traverser

import de.robolab.planet.Direction
import de.robolab.renderer.data.Color
import de.robolab.renderer.theme.LightTheme
import de.robolab.traverser.ITraverserState

val ITraverserState<*>.isCorrect: Boolean
    get() = when {
        statusInfo == null -> true
        (statusInfo as? String)?.startsWith("correct", true) ?: false -> true
        else -> false
    }

class CharacteristicItem(val color: Color) {
    companion object Generator {
        fun generateChararacteristic(state: ITraverserState<*>): Sequence<CharacteristicItem> = state.traceUp().map {
            if (it.status == ITraverserState.Status.ExplorationComplete || it.status == ITraverserState.Status.TargetReached)
                return@map CharacteristicItem(if (it.isCorrect)
                    LightTheme.traverserCharacteristicCorrectColor
                else
                    LightTheme.traverserCharacteristicErrorColor)
            if (!it.running)
                return@map CharacteristicItem(LightTheme.traverserCharacteristicErrorColor)
            return@map when (it.nextDirection) {
                Direction.NORTH -> LightTheme.traverserCharacteristicNorthColor
                Direction.EAST -> LightTheme.traverserCharacteristicEastColor
                Direction.SOUTH -> LightTheme.traverserCharacteristicSouthColor
                Direction.WEST -> LightTheme.traverserCharacteristicWestColor
                else -> null
            }.let { color -> if (color != null) CharacteristicItem(color) else null }
        }.filterNotNull()
    }
}