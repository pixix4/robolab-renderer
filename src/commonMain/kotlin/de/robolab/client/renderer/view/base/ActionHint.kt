package de.robolab.client.renderer.view.base

import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.PointerEvent

class ActionHint(
    val action: Action,
    val description: String
) {

    override fun toString(): String {
        return "$action: $description"
    }

    sealed class Action {
        data class KeyboardAction(
            val keyCode: KeyCode,
            val ctrlKey: Boolean = false,
            val altKey: Boolean = false,
            val shiftKey: Boolean = false,
        ) : Action() {
            override fun toString() = buildString {
                if (ctrlKey) {
                    append("CTRL + ")
                }
                if (altKey) {
                    append("ALT + ")
                }
                if (shiftKey) {
                    append("SHIFT + ")
                }
                append(keyCode.name)
            }
        }

        data class PointerAction(
            val type: PointerEvent.Type,
            val ctrlKey: Boolean = false,
            val altKey: Boolean = false,
            val shiftKey: Boolean = false,
        ) : Action() {
            override fun toString() = buildString {
                if (ctrlKey) {
                    append("CTRL + ")
                }
                if (altKey) {
                    append("ALT + ")
                }
                if (shiftKey) {
                    append("SHIFT + ")
                }
                append(type.name)
            }
        }
    }

    companion object {
        fun key(
            description: String,
            keyCode: KeyCode,
            ctrlKey: Boolean = false,
            altKey: Boolean = false,
            shiftKey: Boolean = false,
        ) = ActionHint(
            Action.KeyboardAction(keyCode, ctrlKey, altKey, shiftKey),
            description
        )

        fun pointer(
            description: String,
            type: PointerEvent.Type,
            ctrlKey: Boolean = false,
            altKey: Boolean = false,
            shiftKey: Boolean = false,
        ) = ActionHint(
            Action.PointerAction(type, ctrlKey, altKey, shiftKey),
            description
        )
    }
}

fun List<ActionHint>.import(parent: List<ActionHint>): List<ActionHint> {
    if (parent.isEmpty()) {
        return this
    }
    return (this + parent).distinctBy { it.action }
}
