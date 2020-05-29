package de.robolab.client.communication

import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import kotlinx.serialization.*

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
        null
    )
) {

    fun parsePlanet() = (payload::planetName.parsed()) to (payload::startX.parsed() to payload::startY.parsed())

    fun parsePath() = Path(
        Coordinate(payload::startX.parsed(), payload::startY.parsed()),
        payload::startDirection.parsed(),
        Coordinate(payload::endX.parsed(), payload::endY.parsed()),
        payload::endDirection.parsed(),
        if (from == From.CLIENT) null else payload::pathWeight.parsed(),
        emptySet(),
        emptyList(),
        hidden = false,
        showDirectionArrow = false
    )

    fun parseStartPoint() = Coordinate(payload::startX.parsed(), payload::startY.parsed())

    fun parseStartOrientation() = payload::startOrientation.parsed()

    fun parseTarget() = Coordinate(payload::targetX.parsed(), payload::targetY.parsed())

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
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.ReadyMessage(metadata)
        }
    },

    @SerialName("planet")
    PLANET {
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
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.PLANET, this)
            message.requireFrom(From.SERVER)
            return RobolabMessage.TargetMessage(
                metadata,
                Coordinate(message.payload::targetX.parsed(), message.payload::targetY.parsed())
            )
        }
    },

    @SerialName("targetReached")
    TARGET_REACHED {
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.TargetReachedMessage(
                metadata,
                message.payload::message orElse ""
            )
        }
    },

    @SerialName("explorationCompleted")
    EXPLORATION_COMPLETED {
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.EXPLORER, this)
            message.requireFrom(From.CLIENT)
            return RobolabMessage.ExplorationCompletedMessage(
                metadata,
                message.payload::message orElse ""
            )
        }
    },

    @SerialName("done")
    DONE {
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
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            val bluePoint =
                if (message.payload.message == "firstBluePoint" && message.payload.startX != null && message.payload.startY != null) {
                    Coordinate(message.payload.startX, message.payload.startY)
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
        override fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage {
            metadata.requireTopic(Topic.CONTROLLER, this)
            message.requireFrom(From.DEBUG)
            return RobolabMessage.DebugMessage(metadata, message.payload::message.parsed(), null)
        }
    };

    abstract fun parseMessage(metadata: RobolabMessage.Metadata, message: JsonMessage): RobolabMessage
}

@Serializable
data class Payload(
    val planetName: String? = null,
    val startX: Int? = null,
    val startY: Int? = null,
    @Serializable(with = DirectionSerializer::class)
    val startDirection: Direction? = null,
    @Serializable(with = DirectionSerializer::class)
    val startOrientation: Direction? = null,
    val endX: Int? = null,
    val endY: Int? = null,
    @Serializable(with = DirectionSerializer::class)
    val endDirection: Direction? = null,
    val targetX: Int? = null,
    val targetY: Int? = null,
    val pathStatus: PathStatus? = null,
    val pathWeight: Int? = null,
    val message: String? = null,
    val debug: String? = null
)

@Serializable
enum class PathStatus {
    @SerialName("free")
    FREE,

    @SerialName("blocked")
    BLOCKED
}

@Serializer(forClass = Direction::class)
object DirectionSerializer : KSerializer<Direction> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("WithCustomDefault", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Direction) {
        val int = when (value) {
            Direction.NORTH -> 0
            Direction.EAST -> 90
            Direction.SOUTH -> 180
            Direction.WEST -> 270
        }

        encoder.encodeInt(int)
    }

    override fun deserialize(decoder: Decoder): Direction {
        return when (decoder.decodeInt()) {
            0 -> Direction.NORTH
            90 -> Direction.EAST
            180 -> Direction.SOUTH
            270 -> Direction.WEST
            else -> Direction.NORTH
        }
    }
}
