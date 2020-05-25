package de.robolab.client.app.model.traverser

import de.robolab.client.traverser.ITraverserState
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.planet.Direction
import de.robolab.common.utils.Color

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
                    return@map CharacteristicItem(
                        if (it.isCorrect)
                            theme.traverser.traverserCharacteristicCorrectColor
                        else
                            theme.traverser.traverserCharacteristicErrorColor
                    )
                if (!it.running)
                    return@map CharacteristicItem(theme.traverser.traverserCharacteristicErrorColor)
                return@map when (it.nextDirection) {
                    Direction.NORTH -> theme.traverser.traverserCharacteristicNorthColor
                    Direction.EAST -> theme.traverser.traverserCharacteristicEastColor
                    Direction.SOUTH -> theme.traverser.traverserCharacteristicSouthColor
                    Direction.WEST -> theme.traverser.traverserCharacteristicWestColor
                    else -> null
                }.let { color -> if (color != null) CharacteristicItem(color) else null }
            }.filterNotNull().toList().asReversed()
        }
    }
}