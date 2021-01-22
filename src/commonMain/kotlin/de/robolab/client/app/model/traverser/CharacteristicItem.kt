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
        fun createCharacteristicTrace(state: ITraverserState<*>): List<CharacteristicItem> {
            return state.traceUp().map(::createCharacteristic).filterNotNull().toList().asReversed()
        }

        fun createCharacteristic(state: ITraverserState<*>): CharacteristicItem? {
            val theme = PreferenceStorage.selectedTheme.theme
            if (state.status == ITraverserState.Status.ExplorationComplete || state.status == ITraverserState.Status.TargetReached)
                return CharacteristicItem(
                    if (state.isCorrect)
                        theme.traverser.traverserCharacteristicCorrectColor
                    else
                        theme.traverser.traverserCharacteristicErrorColor
                )
            if (!state.running)
                return CharacteristicItem(theme.traverser.traverserCharacteristicErrorColor)
            return when (state.nextDirection) {
                Direction.NORTH -> theme.traverser.traverserCharacteristicNorthColor
                Direction.EAST -> theme.traverser.traverserCharacteristicEastColor
                Direction.SOUTH -> theme.traverser.traverserCharacteristicSouthColor
                Direction.WEST -> theme.traverser.traverserCharacteristicWestColor
                else -> null
            }.let { color -> if (color != null) CharacteristicItem(color) else null }
        }
    }
}