package de.robolab.client.app.controller

import de.robolab.client.communication.*
import de.robolab.common.planet.Direction
import de.robolab.common.utils.RobolabJson
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SendMessageController(topic: String, private val sendMessage: (String, String) -> Boolean) {

    val topicProperty = property(topic)
    var topic by topicProperty
    val fromProperty = property(From.SERVER)
    var from by fromProperty
    val typeProperty = property(Type.PathMessage)
    var type by typeProperty

    enum class Type(val displayName: String) {
        PathSelectMessage("Path select"),
        PathMessage("Path"),
        ReadyMessage("Ready"),
        PlanetMessage("Planet"),
        SetPlanetMessage("Set planet (exam)"),
        TestPlanetMessage("Test planet"),
        PathUnveilMessage("Path unveil"),
        TargetMessage("Target"),
        TargetReachedMessage("Target reached"),
        ExplorationCompletedMessage("Exploration completed"),
        DoneMessage("Done"),
        CustomMessage("Custom");

        override fun toString(): String {
            return this.displayName
        }
    }

    enum class From {
        CLIENT,
        SERVER;

        fun convert() = when (this) {
            CLIENT -> de.robolab.client.communication.From.CLIENT
            SERVER -> de.robolab.client.communication.From.SERVER
        }
    }

    val planetNameProperty = property<String>()
    var planetName by planetNameProperty
    val planetNameVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage || it == Type.SetPlanetMessage || it == Type.TestPlanetMessage
    }

    val startXProperty = property<Int>()
    var startX by startXProperty
    val startXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startYProperty = property<Int>()
    var startY by startYProperty
    val startYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startDirectionProperty = property<Direction>()
    var startDirection by startDirectionProperty
    val startDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val startOrientationProperty = property<Direction>()
    var startOrientation by startOrientationProperty
    val startOrientationVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage
    }

    val endXProperty = property<Int>()
    var endX by endXProperty
    val endXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endYProperty = property<Int>()
    var endY by endYProperty
    val endYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endDirectionProperty = property<Direction>()
    var endDirection by endDirectionProperty
    val endDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val targetXProperty = property<Int>()
    var targetX by targetXProperty
    val targetXVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val targetYProperty = property<Int>()
    var targetY by targetYProperty
    val targetYVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val pathStatusProperty = property<PathStatus>()
    var pathStatus by pathStatusProperty
    val pathStatusVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val pathWeightProperty = property<Int>()
    var pathWeight by pathWeightProperty
    val pathWeightVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val messageProperty = property<String>()
    var message by messageProperty
    val messageVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetReachedMessage || it == Type.ExplorationCompletedMessage || it == Type.DoneMessage
    }

    val customProperty = property<String>()
    var custom by customProperty
    val customVisibleProperty = typeProperty.mapBinding {
        it == Type.CustomMessage
    }

    fun send(): Boolean {
        val message = when (typeProperty.value) {
            Type.PathSelectMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.PATH_SELECT,
                Payload(
                    startX = startXProperty.value,
                    startY = startYProperty.value,
                    startDirection = startDirectionProperty.value
                )
            )
            Type.PathMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.PATH,
                Payload(
                    startX = startXProperty.value,
                    startY = startYProperty.value,
                    startDirection = startDirectionProperty.value,
                    endX = startXProperty.value,
                    endY = startYProperty.value,
                    endDirection = startDirectionProperty.value,
                    pathStatus = pathStatusProperty.value,
                    pathWeight = pathWeightProperty.value
                )
            )
            Type.ReadyMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.READY,
                Payload()
            )
            Type.SetPlanetMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.SET_PLANET,
                Payload(
                    planetName = planetNameProperty.value
                )
            )
            Type.TestPlanetMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.TEST_PLANET,
                Payload(
                    planetName = planetNameProperty.value
                )
            )
            Type.PlanetMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.PLANET,
                Payload(
                    planetName = planetNameProperty.value,
                    startX = startXProperty.value,
                    startY = startYProperty.value,
                    startOrientation = startOrientationProperty.value
                )
            )
            Type.PathUnveilMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.PATH_UNVEILED,
                Payload(
                    startX = startXProperty.value,
                    startY = startYProperty.value,
                    startDirection = startDirectionProperty.value,
                    endX = startXProperty.value,
                    endY = startYProperty.value,
                    endDirection = startDirectionProperty.value,
                    pathStatus = pathStatusProperty.value,
                    pathWeight = pathWeightProperty.value
                )
            )
            Type.TargetMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.TARGET,
                Payload(
                    targetX = targetXProperty.value,
                    targetY = targetYProperty.value
                )
            )
            Type.TargetReachedMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.TARGET_REACHED,
                Payload(
                    message = messageProperty.value
                )
            )
            Type.ExplorationCompletedMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.EXPLORATION_COMPLETED,
                Payload(
                    message = messageProperty.value
                )
            )
            Type.DoneMessage -> JsonMessage(
                fromProperty.value.convert(),
                de.robolab.client.communication.Type.DONE,
                Payload(
                    message = messageProperty.value
                )
            )
            Type.CustomMessage -> {
                return sendMessage(
                    topicProperty.value,
                    customProperty.value ?: ""
                )
            }
        }

        return sendMessage(
            topicProperty.value,
            RobolabJson.encodeToString(JsonMessage.serializer(), message)
        )
    }
}
