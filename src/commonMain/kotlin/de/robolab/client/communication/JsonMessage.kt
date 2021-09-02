package de.robolab.client.communication

import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPath
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class JsonMessage(
    val from: From,
    val type: Type,
    val payload: Payload = Payload(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
) {

    fun parsePlanet() = (payload::planetName.parsed()) to (payload::startX.parsed() to payload::startY.parsed())

    fun parsePath() = PlanetPath(
        source = PlanetPoint(payload::startX.parsed(), payload::startY.parsed()),
        sourceDirection = payload::startDirection.parsed(),
        target = PlanetPoint(payload::endX.parsed(), payload::endY.parsed()),
        targetDirection = payload::endDirection.parsed(),
        weight = if (from == From.CLIENT) (if (payload.pathStatus == PathStatus.BLOCKED) -1L else 0L) else payload::pathWeight.parsed(),
        exposure = emptySet(),
        hidden = false,
        spline = null,
        arrow = false,
    )

    fun parseStartPoint() = PlanetPoint(payload::startX.parsed(), payload::startY.parsed())

    fun parseStartOrientation() = payload::startOrientation.parsed()

    fun parseTarget() = PlanetPoint(payload::targetX.parsed(), payload::targetY.parsed())

    fun requireFrom(vararg requiredFrom: From) {
        if (requiredFrom.none { it == from }) {
            throw IllegalFromException(from, type)
        }
    }
}

@Serializable
enum class From {
    @SerialName("client")
    CLIENT,

    @SerialName("server")
    SERVER,

    @SerialName("debug")
    DEBUG,
    UNKNOWN
}

/*
controller
planet
explorer
 */


/* Possible message types:
explorer ready: from=client
explorer planet: from=server; planetName, startX, startY
planet path: from=client; startX, startY, startDirection, endX, endY, endDirection, pathStatus
planet path: from=server; startX, startY, startDirection, endX, endY, endDirection, pathStatus, pathWeight
planet pathSelect: from=client; startX, startY, startDirection
planet pathSelect: from=server; startDirection
planet pathUnveiled; from=server; startX, startY, startDirection, endX, endY, endDirection, pathStatus, pathWeight
planet target: from=server; targetX, targetY
planet targetReached: from=client; message
planet explorationCompleted; from=client; message
explorer testplanet: from=client; planetName
controller notice: from=debug; message
controller error: from=debug; message, debug
controller adjust: from=debug; message
 */

@Serializable
enum class Type {
    @SerialName("ready")
    READY {
        override val serialName = "ready"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.ReadyMessage(metadata)
        }
    },

    @SerialName("planet")
    PLANET {
        override val serialName = "planet"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.SERVER)
            return RobolabMessage.PlanetMessage(
                metadata,
                message.payload::planetName.parsed(),
                message.parseStartPoint(),
                message.parseStartOrientation()
            )
        }
    },

    @SerialName("setPlanet")
    SET_PLANET {
        override val serialName = "setPlanet"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            val planetName = message.payload.planetName ?: message.payload.message?.let {
                ".* planet set to (.*) for .*".toRegex().matchEntire(it)?.groupValues?.getOrNull(1)
            }
            return RobolabMessage.SetPlanetMessage(
                metadata,
                planetName ?: message.payload::planetName.parsed()
            )
        }
    },

    @SerialName("path")
    PATH {
        override val serialName = "path"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.PLANET, this)
            message.requireFrom(From.SERVER, From.CLIENT)
            return RobolabMessage.FoundPathMessage(
                metadata,
                message.parsePath()
            )
        }
    },

    @SerialName("pathSelect")
    PATH_SELECT {
        override val serialName = "pathSelect"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.PLANET, this)
            message.requireFrom(From.CLIENT, From.SERVER)
            return if (message.from == From.CLIENT) {
                RobolabMessage.PathSelectMessageFromRobot(
                    metadata,
                    message.parseStartPoint(),
                    message.payload::startDirection.parsed()
                )
            } else RobolabMessage.PathSelectMessageFromServer(
                metadata,
                message.payload::startDirection.parsed()
            )
        }
    },

    @SerialName("pathUnveiled")
    PATH_UNVEILED {
        override val serialName = "pathUnveiled"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.PLANET, this)
            message.requireFrom(From.SERVER)
            return RobolabMessage.PathUnveiledMessage(
                metadata,
                message.parsePath()
            )
        }
    },

    @SerialName("target")
    TARGET {
        override val serialName = "target"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.PLANET, this)
            message.requireFrom(From.SERVER)
            return RobolabMessage.TargetMessage(
                metadata,
                PlanetPoint(message.payload::targetX.parsed(), message.payload::targetY.parsed())
            )
        }
    },

    @SerialName("targetReached")
    TARGET_REACHED {
        override val serialName = "targetReached"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.TargetReachedMessage(
                metadata,
                message.payload::message.orElse("", metadata.groupId)
            )
        }
    },

    @SerialName("explorationCompleted")
    EXPLORATION_COMPLETED {
        override val serialName = "explorationCompleted"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.ExplorationCompletedMessage(
                metadata,
                message.payload::message.orElse("", metadata.groupId)
            )
        }
    },

    @SerialName("done")
    DONE {
        override val serialName = "done"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.SERVER)
            return RobolabMessage.DoneMessage(
                metadata,
                message.payload::message.parsed()
            )
        }
    },

    @SerialName("testplanet")
    TEST_PLANET {
        override val serialName = "testplanet"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.TestplanetMessage(
                metadata,
                message.payload::planetName.parsed()
            )
        }
    },

    @SerialName("adjust")
    ADJUST {
        override val serialName = "adjust"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            return RobolabMessage.DebugMessage(
                metadata,
                with(message.payload) { "$startX,$startY,${startDirection} $endX,$endY,${endDirection}" },
                null
            )
        }
    },

    @SerialName("reload")
    RELOAD {
        override val serialName = "reload"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            return RobolabMessage.DebugMessage(
                metadata,
                "Reload requested",
                null
            )
        }
    },

    @SerialName("notice")
    NOTICE {
        override val serialName = "notice"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            val bluePoint =
                if (message.payload.message == "firstBluePoint" && message.payload.startX != null && message.payload.startY != null) {
                    PlanetPoint(message.payload.startX, message.payload.startY)
                } else null
            return RobolabMessage.DebugMessage(
                metadata,
                message.payload::message.parsed(),
                bluePoint
            )
        }
    },

    @SerialName("error")
    ERROR {
        override val serialName = "error"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            return RobolabMessage.DebugMessage(metadata, message.payload::message.parsed(), null)
        }
    },

    @SerialName("syntax")
    SYNTAX {
        override val serialName = "syntax"
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.COMTEST, this)
            message.requireFrom(From.DEBUG)
            return RobolabMessage.SyntaxMessage(
                metadata,
                message.payload::message.parsed(),
                message.payload.errors ?: emptyList(),
            )
        }
    };

    abstract fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage

    abstract val serialName: String
}

@Serializable
data class Payload(
    val planetName: String? = null,
    val startX: Long? = null,
    val startY: Long? = null,
    @Serializable(with = DirectionSerializer::class)
    val startDirection: PlanetDirection? = null,
    @Serializable(with = DirectionSerializer::class)
    val startOrientation: PlanetDirection? = null,
    val endX: Long? = null,
    val endY: Long? = null,
    @Serializable(with = DirectionSerializer::class)
    val endDirection: PlanetDirection? = null,
    val targetX: Long? = null,
    val targetY: Long? = null,
    val pathStatus: PathStatus? = null,
    val pathWeight: Long? = null,
    val message: String? = null,
    val debug: String? = null,
    val errors: List<String>? = null
)

@Serializable
enum class PathStatus {
    @SerialName("free")
    FREE,

    @SerialName("blocked")
    BLOCKED
}

object DirectionSerializer : KSerializer<PlanetDirection> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WithCustomDefault", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PlanetDirection) {
        val int = when (value) {
            PlanetDirection.North -> 0
            PlanetDirection.East -> 90
            PlanetDirection.South -> 180
            PlanetDirection.West -> 270
        }

        encoder.encodeInt(int)
    }

    override fun deserialize(decoder: Decoder): PlanetDirection {
        return when (decoder.decodeInt()) {
            0 -> PlanetDirection.North
            90 -> PlanetDirection.East
            180 -> PlanetDirection.South
            270 -> PlanetDirection.West
            else -> PlanetDirection.North
        }
    }
}
