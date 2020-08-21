package de.robolab.client.communication

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path

/**
 * @author leon
 */
sealed class RobolabMessage(
        val metadata: Metadata
) {

    abstract val summary: String
    abstract val details: List<Pair<String, String>>

    data class Metadata(
        val time: Long,
        val groupId: String,
        val from: From,
        val topic: String,
        val rawMessage: String
    )

    abstract class PathMessage(
        metadata: Metadata,
        val path: Path
    ) : RobolabMessage(metadata)

    class FoundPathMessage(
        metadata: Metadata,
        path: Path
    ) : PathMessage(metadata, path) {
        override val summary by lazy { "${metadata.comTestString}Path: ${pathToString(path)}" }
        override val details by lazy { pathToDetails(path) }
    }

    class PathSelectMessageFromRobot(
        metadata: Metadata,
        val point: Coordinate,
        val direction: Direction
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Path selected: direction=${direction.export()} (at point ${point.x},${point.y})" }
        override val details by lazy {
            listOf(
                    "Point" to "${point.x},${point.y}",
                    "Direction" to direction.name
            )
        }
    }

    class PathSelectMessageFromServer(
        metadata: Metadata,
        val direction: Direction
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Select path: direction=$direction" }
        override val details by lazy {
            listOf(
                    "Direction" to direction.name
            )
        }
    }

    class PathUnveiledMessage(
        metadata: Metadata,
        path: Path
    ) : PathMessage(metadata, path) {
        override val summary by lazy { "${metadata.comTestString}Path unveiled: ${pathToString(path)}" }
        override val details by lazy {
            pathToDetails(path)
        }
    }

    class PlanetMessage(
        metadata: Metadata,
        val planetName: String,
        val startPoint: Coordinate,
        val startOrientation: Direction
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Planet: name=$planetName, start=(${startPoint.x},${startPoint.y},${startOrientation.export()})" }
        override val details by lazy {
            listOf(
                    "Planet" to planetName,
                    "Start" to "${startPoint.x}, ${startPoint.y}, ${startOrientation.name}"
            )
        }
    }

    class SetPlanetMessage(
        metadata: Metadata,
        val planetName: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Planet: $planetName" }
        override val details by lazy {
            listOf(
                    "Planet" to planetName
            )
        }
    }

    class TargetMessage(
        metadata: Metadata,
        val target: Coordinate
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Target: ${target.x},${target.y}" }
        override val details by lazy {
            listOf(
                    "Target" to "${target.x}, ${target.y}"
            )
        }
    }

    class TargetReachedMessage(
        metadata: Metadata,
        val message: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}🧭 $message" }
        override val details by lazy {
            listOf(
                    "Message" to message
            )
        }
    }

    class ExplorationCompletedMessage(
        metadata: Metadata,
        val message: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}🏁 $message" }
        override val details by lazy {
            listOf(
                    "Message" to message
            )
        }
    }

    class DoneMessage(
        metadata: Metadata,
        val message: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}✅ $message" }
        override val details by lazy {
            listOf(
                    "Message" to message
            )
        }
    }

    class TestplanetMessage(
        metadata: Metadata,
        val planetName: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Testplanet: $planetName" }
        override val details by lazy {
            listOf(
                    "Test planet" to planetName
            )
        }
    }

    class ReadyMessage(
            metadata: Metadata
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Ready!" }
        override val details by lazy {
            emptyList<Pair<String, String>>()
        }
    }

    class DebugMessage(
        metadata: Metadata,
        val message: String,
        val firstBluePoint: Coordinate?
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Debug: $message" }
        override val details by lazy {
            listOf(
                    "Message" to message
            )
        }
    }

    class SyntaxMessage(
        metadata: Metadata,
        val message: String,
        val errors: List<String>
    ) : RobolabMessage(metadata) {
        val char = if (errors.isEmpty()) "✓" else "✗"
        override val summary by lazy { "$char Syntax: $message" }
        override val details by lazy {
            listOf(
                "Message" to message,
            ) + errors.map {
                "Error" to it
            }
        }
    }

    class IllegalMessage(
        metadata: Metadata,
        val reason: Reason,
        val errorMessage: String? = null
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "Illegal: ${reason.shortMessage}" }
        override val details by lazy {
            val result = mutableListOf(
                    "Reason" to reason.shortMessage
            )
            errorMessage?.let {
                result.add("Error" to it)
            }
            return@lazy result
        }

        sealed class Reason {
            abstract val shortMessage: String

            object WrongTopic : Reason() {
                override val shortMessage = "wrong topic"
            }

            class MissingArgument(val argumentName: String) : Reason() {
                override val shortMessage by lazy { "argument $argumentName is missing" }
            }

            object IllegalFromValue : Reason() {
                override val shortMessage = "illegal \"from\" value"
            }

            object NotParsable : Reason() {
                override val shortMessage = "not parsable"
            }
        }
    }

    override fun toString() = "${DateFormat("HH:mm:ss").format(DateTimeTz.fromUnixLocal(metadata.time))}: ${metadata.rawMessage}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RobolabMessage) return false

        return metadata == other.metadata
    }

    override fun hashCode(): Int {
        return metadata.hashCode()
    }

    companion object {
        fun pathToString(path: Path) = with(path) {
            "(${source.x},${source.y},${sourceDirection.export()}) → " +
                    "(${target.x},${target.y},${targetDirection.export()})" +
                    (if (blocked) ", blocked" else "") +
                    if (this.weight != null) ", weight: $weight" else ""
        }

        fun pathToDetails(path: Path): List<Pair<String, String>> = with(path) {
            val result = mutableListOf(
                    "Start" to "${source.x}, ${source.y}, ${sourceDirection.name}",
                    "End" to "${target.x}, ${target.y}, ${targetDirection.name}",
                    "Status" to if (this.blocked) "blocked" else "free"
            )
            weight?.let {
                result += "Weight" to it.toString()
            }

            return result
        }
    }

    val Metadata.comTestString:String
            get() = if (topic.startsWith(Topic.COMTEST.topicName)) "COMTEST: " else ""
}

fun Direction.export() = when (this) {
    Direction.NORTH -> "N"
    Direction.EAST -> "E"
    Direction.SOUTH -> "S"
    Direction.WEST -> "W"
}
