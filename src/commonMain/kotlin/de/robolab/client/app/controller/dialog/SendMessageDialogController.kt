package de.robolab.client.app.controller.dialog

import de.robolab.client.communication.*
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.utils.Logger
import de.robolab.common.utils.RobolabJson
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

    fun topicControllerAdmin() {
        topic = "controller/000"
    }

    fun topicControllerGroup() {
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
            Type.ControllerSetPlanetMessage -> {
                topicControllerGroup()
                from = From.ADMIN
            }
            Type.ControllerGetPlanetsMessage -> {
                topicControllerAdmin()
                from = From.ADMIN
            }
            Type.ControllerReloadMessage -> {
                topicControllerAdmin()
                from = From.ADMIN
            }
            Type.ControllerDumpGroupMessage -> {
                topicControllerAdmin()
                from = From.ADMIN
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
        ControllerSetPlanetMessage("[controller] Set planet"),
        ControllerReloadMessage("[controller] Reload planets"),
        ControllerGetPlanetsMessage("[controller] Get available planets"),
        ControllerDumpGroupMessage("[controller] Dump group state"),
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
        SERVER,
        ADMIN;

        fun convert() = when (this) {
            CLIENT -> de.robolab.client.communication.From.CLIENT
            SERVER -> de.robolab.client.communication.From.SERVER
            ADMIN -> de.robolab.client.communication.From.ADMIN
        }
    }

    val planetNameProperty = property("")
    var planetName by planetNameProperty
    val planetNameVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage || it == Type.ControllerSetPlanetMessage || it == Type.TestPlanetMessage
    }

    val startXProperty = property(0L)
    var startX by startXProperty
    val startXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startYProperty = property(0L)
    var startY by startYProperty
    val startYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PlanetMessage || it == Type.PathUnveilMessage
    }

    val startDirectionProperty = property(PlanetDirection.North)
    var startDirection by startDirectionProperty
    val startDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathSelectMessage || it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val startOrientationProperty = property(PlanetDirection.North)
    var startOrientation by startOrientationProperty
    val startOrientationVisibleProperty = typeProperty.mapBinding {
        it == Type.PlanetMessage
    }

    val endXProperty = property(0L)
    var endX by endXProperty
    val endXVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endYProperty = property(0L)
    var endY by endYProperty
    val endYVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val endDirectionProperty = property(PlanetDirection.North)
    var endDirection by endDirectionProperty
    val endDirectionVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val targetXProperty = property(0L)
    var targetX by targetXProperty
    val targetXVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val targetYProperty = property(0L)
    var targetY by targetYProperty
    val targetYVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetMessage
    }

    val pathStatusProperty = property(PathStatus.FREE)
    var pathStatus by pathStatusProperty
    val pathStatusVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val pathWeightProperty = property(1L)
    var pathWeight by pathWeightProperty
    val pathWeightVisibleProperty = typeProperty.mapBinding {
        it == Type.PathMessage || it == Type.PathUnveilMessage
    }

    val messageProperty = property("")
    var message by messageProperty
    val messageVisibleProperty = typeProperty.mapBinding {
        it == Type.TargetReachedMessage || it == Type.ExplorationCompletedMessage || it == Type.DoneMessage
    }

    val groupIdProperty = property("")
    var groupId by groupIdProperty
    val groupIdVisibleProperty = typeProperty.mapBinding {
        it == Type.ControllerDumpGroupMessage
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
            Type.ControllerSetPlanetMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.SET_PLANET,
                Payload(
                    planetName = planetName
                )
            )
            Type.ControllerGetPlanetsMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.GET_PLANETS,
                Payload()
            )
            Type.ControllerReloadMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.RELOAD,
                Payload()
            )
            Type.ControllerDumpGroupMessage -> JsonMessage(
                from.convert(),
                de.robolab.client.communication.Type.DUMP_GROUP,
                Payload(
                    groupId = groupId
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

        Logger(this).info { "${topicProperty.value}: $message" }

        val jsonMessage = buildJsonObject {
            put("from", message.from.name.lowercase())
            put("type", message.type.serialName)
            put("payload", buildJsonObject {
                if (message.payload.planetName != null) {
                    put("planetName", message.payload.planetName)
                }
                if (message.payload.startX != null) {
                    put("startX", message.payload.startX)
                }
                if (message.payload.startY != null) {
                    put("startY", message.payload.startY)
                }
                if (message.payload.startDirection != null) {
                    put("startDirection", message.payload.startDirection.let { value ->
                        when (value) {
                            PlanetDirection.North -> 0
                            PlanetDirection.East -> 90
                            PlanetDirection.South -> 180
                            PlanetDirection.West -> 270
                        }
                    })
                }
                if (message.payload.startOrientation != null) {
                    put("startOrientation", message.payload.startOrientation.let { value ->
                        when (value) {
                            PlanetDirection.North -> 0
                            PlanetDirection.East -> 90
                            PlanetDirection.South -> 180
                            PlanetDirection.West -> 270
                        }
                    })
                }
                if (message.payload.endX != null) {
                    put("endX", message.payload.endX)
                }
                if (message.payload.endY != null) {
                    put("endY", message.payload.endY)
                }
                if (message.payload.endDirection != null) {
                    put("endDirection", message.payload.endDirection.let { value ->
                        when (value) {
                            PlanetDirection.North -> 0
                            PlanetDirection.East -> 90
                            PlanetDirection.South -> 180
                            PlanetDirection.West -> 270
                        }
                    })
                }
                if (message.payload.targetX != null) {
                    put("targetX", message.payload.targetX)
                }
                if (message.payload.targetY != null) {
                    put("targetY", message.payload.targetY)
                }
                if (message.payload.pathStatus != null) {
                    put("pathStatus", message.payload.pathStatus.name.lowercase())
                }
                if (message.payload.pathWeight != null) {
                    put("pathWeight", message.payload.pathWeight)
                }
                if (message.payload.message != null) {
                    put("message", message.payload.message)
                }
                if (message.payload.debug != null) {
                    put("debug", message.payload.debug)
                }
                if (message.payload.groupId != null) {
                    put("groupId", message.payload.groupId)
                }
            })
        }

        return messageManager.sendMessage(
            topicProperty.value,
            RobolabJson.encodeToString(jsonMessage)
        )
    }
}
