package de.robolab.client.app.controller.dialog

import de.robolab.client.communication.*
import de.robolab.common.planet.Direction
import de.robolab.common.utils.RobolabJson
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class SendMessageDialogController(
    private val groupNumber: String,
    private val planet: String,
    private val messageManager: MessageManager
) {

    val topicProperty = property("")
    var topic by topicProperty
    val fromProperty = property(From.SERVER)
    var from by fromProperty
    val typeProperty = property(Type.PathMessage)
    var type by typeProperty

    fun topicExplorer() {
        topic = "explorer/$groupNumber"
    }

    fun topicPlanet() {
        topic = "planet/$planet/$groupNumber"
    }

    fun topicController() {
        topic = "controller/$groupNumber"
    }

    fun topicByType() {
        when (type) {
            Type.PathSelectMessage -> {
                topicPlanet()
                from = From.CLIENT
            }
            Type.PathMessage -> {
                topicPlanet()
                from = From.CLIENT
            }
            Type.ReadyMessage -> {
                topicExplorer()
                from = From.CLIENT
            }
            Type.PlanetMessage -> {
                topicExplorer()
                from = From.SERVER
            }
            Type.SetPlanetMessage -> {
                topicController()
                from = From.CLIENT
            }
            Type.TestPlanetMessage -> {
                topicExplorer()
                from = From.CLIENT
            }
            Type.PathUnveilMessage -> {
                topicPlanet()
                from = From.CLIENT
            }
            Type.TargetMessage -> {
                topicPlanet()
                from = From.CLIENT
            }
            Type.TargetReachedMessage -> {
                topicExplorer()
                from = From.CLIENT
            }
            Type.ExplorationCompletedMessage -> {
                topicExplorer()
                from = From.CLIENT
            }
            Type.DoneMessage -> {
                topicExplorer()
                from = From.SERVER
            }
            Type.CustomMessage -> {
            }
        }
    }

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

    val planetNameProperty = property("")
    var planetName by planetNameProperty
    val planetNameVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage || it == Type.SetPlanetMessage || it == Type.TestPlanetMessage
    }

    val startXProperty = property(0)
    var startX by startXProperty
    val startXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startYProperty = property(0)
    var startY by startYProperty
    val startYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startDirectionProperty = property(Direction.NORTH)
    var startDirection by startDirectionProperty
    val startDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val startOrientationProperty = property(Direction.NORTH)
    var startOrientation by startOrientationProperty
    val startOrientationVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage
    }

    val endXProperty = property(0)
    var endX by endXProperty
    val endXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endYProperty = property(0)
    var endY by endYProperty
    val endYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endDirectionProperty = property(Direction.NORTH)
    var endDirection by endDirectionProperty
    val endDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val targetXProperty = property(0)
    var targetX by targetXProperty
    val targetXVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val targetYProperty = property(0)
    var targetY by targetYProperty
    val targetYVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val pathStatusProperty = property(PathStatus.FREE)
    var pathStatus by pathStatusProperty
    val pathStatusVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val pathWeightProperty = property(1)
    var pathWeight by pathWeightProperty
    val pathWeightVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val messageProperty = property("")
    var message by messageProperty
    val messageVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetReachedMessage || it == Type.ExplorationCompletedMessage || it == Type.DoneMessage
    }

    val customProperty = property("")
    var custom by customProperty
    val customVisibleProperty = typeProperty.mapBinding {
        it == Type.CustomMessage
    }

    fun send(): Boolean {
        val message = when (typeProperty.value) {
            Type.PathSelectMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.PATH_SELECT,
                Payload(
                    startX = startX,
                    startY = startY,
                    startDirection = startDirection
                )
            )
            Type.PathMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.PATH,
                Payload(
                    startX = startX,
                    startY = startY,
                    startDirection = startDirection,
                    endX = endX,
                    endY = endY,
                    endDirection = endDirection,
                    pathStatus = pathStatus,
                    pathWeight = pathWeight
                )
            )
            Type.ReadyMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.READY,
                Payload()
            )
            Type.SetPlanetMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.SET_PLANET,
                Payload(
                    planetName = planetName
                )
            )
            Type.TestPlanetMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.TEST_PLANET,
                Payload(
                    planetName = planetName
                )
            )
            Type.PlanetMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.PLANET,
                Payload(
                    planetName = planetName,
                    startX = startX,
                    startY = startY,
                    startOrientation = startOrientation
                )
            )
            Type.PathUnveilMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.PATH_UNVEILED,
                Payload(
                    startX = startX,
                    startY = startY,
                    startDirection = startDirection,
                    endX = endX,
                    endY = endY,
                    endDirection = endDirection,
                    pathStatus = pathStatus,
                    pathWeight = pathWeight
                )
            )
            Type.TargetMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.TARGET,
                Payload(
                    targetX = targetX,
                    targetY = targetY
                )
            )
            Type.TargetReachedMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.TARGET_REACHED,
                Payload(
                    message = message
                )
            )
            Type.ExplorationCompletedMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.EXPLORATION_COMPLETED,
                Payload(
                    message = message
                )
            )
            Type.DoneMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.DONE,
                Payload(
                    message = message
                )
            )
            Type.CustomMessage -> {
                return messageManager.sendMessage(
                    topic,
                    custom
                )
            }
        }

        return messageManager.sendMessage(
            topicProperty.value,
            RobolabJson.encodeToString(JsonMessage.serializer(), message)
        )
    }
}
