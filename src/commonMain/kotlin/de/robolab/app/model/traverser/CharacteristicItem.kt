package de.robolab.app.model.traverser

import de.robolab.planet.Direction
import de.robolab.renderer.data.Color
import de.robolab.traverser.ITraverserState
import de.robolab.utils.PreferenceStorage

val ITraverserState<*>.isCorrect: Boolean
    get() = when {
        statusInfo == null -> true
        (statusInfo as? String)?.startsWith("correct", true) ?: false -> true
        else -> false
    }

class CharacteristicItem(val color: Color) {
    companion object Generator {
        fun generateCharacteristic(state: ITraverserState<*>): List<CharacteristicItem> {
            val theme = PreferenceStorage.selectedTheme.theme
            
            return state.traceUp().map {
                if (it.status == ITraverserState.Status.ExplorationComplete || it.status == ITraverserState.Status.TargetReached)
                    return@map CharacteristicItem(if (it.isCorrect)
                        theme.traverserCharacteristicCorrectColor
                    else
                        theme.traverserCharacteristicErrorColor)
                if (!it.running)
                    return@map CharacteristicItem(theme.traverserCharacteristicErrorColor)
                return@map when (it.nextDirection) {
                    Direction.NORTH -> theme.traverserCharacteristicNorthColor
                    Direction.EAST -> theme.traverserCharacteristicEastColor
                    Direction.SOUTH -> theme.traverserCharacteristicSouthColor
                    Direction.WEST -> theme.traverserCharacteristicWestColor
                    else -> null
                }.let { color -> if (color != null) CharacteristicItem(color) else null }
            }.filterNotNull().toList().asReversed()
        }
    }
}