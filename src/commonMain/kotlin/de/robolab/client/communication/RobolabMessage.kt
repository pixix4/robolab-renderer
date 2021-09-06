package de.robolab.client.communication

import de.robolab.client.utils.Flags
import de.robolab.client.utils.emptyFlags
import de.robolab.common.net.data.OdometryData
import de.robolab.common.net.data.OdometryPayloadFlags
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.utils.formatDateTime

/**
 * @author leon
 */
sealed class RobolabMessage(
    val metadata: Metadata,
) {

    abstract val summary: String
    abstract val details: List<Pair<String, String>>

    data class Metadata(
        val time: Long,
        val groupId: String,
        val from: From,
        val topic: String,
        val rawMessage: String,
    )

    abstract class PathMessage(
        metadata: Metadata,
        val path: PlanetPath,
    ) : RobolabMessage(metadata)

    class FoundPathMessage(
        metadata: Metadata,
        path: PlanetPath,
    ) : PathMessage(metadata, path) {
        override val summary by lazy { "${metadata.comTestString}Path: ${pathToString(path, metadata.from)}" }
        override val details by lazy { pathToDetails(path, metadata.from) }
    }

    class PathSelectMessageFromRobot(
        metadata: Metadata,
        val point: PlanetPoint,
        val direction: PlanetDirection,
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
        val direction: PlanetDirection,
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
        path: PlanetPath,
    ) : PathMessage(metadata, path) {
        override val summary by lazy { "${metadata.comTestString}Path unveiled: ${pathToString(path, metadata.from)}" }
        override val details by lazy {
            pathToDetails(path, metadata.from)
        }
    }

    class PlanetMessage(
        metadata: Metadata,
        val planetName: String,
        val startPoint: PlanetPoint,
        val startOrientation: PlanetDirection,
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
        val planetName: String,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Planet: $planetName" }
        override val details by lazy {
            listOf(
                "Planet" to planetName
            )
        }
    }

    class DefaultPlanetMessage(
        metadata: Metadata,
        val message: String,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Planet: $message" }
        override val details by lazy {
            listOf(
                "Message" to message
            )
        }
    }

    class ReloadMessage(
        metadata: Metadata,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Reload" }
        override val details by lazy {
            emptyList<Pair<String, String>>()
        }
    }

    class ActivePlanetsMessage(
        metadata: Metadata,
        val planets: List<String>
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}${planets.size} planet(s) found" }
        override val details by lazy {
            listOf(
                "Planets" to planets.joinToString("\n")
            )
        }
    }

    class GetPlanetsMessage(
        metadata: Metadata,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Get Planets" }

        override val details by lazy {
            emptyList<Pair<String, String>>()
        }
    }

    class DumpGroupMessage(
        metadata: Metadata,
        val groupId: String
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Dump Group $groupId" }
        override val details by lazy {
            listOf(
                "Group" to groupId
            )
        }
    }

    class GroupDumpMessage(
        metadata: Metadata,
        val groupId: String,
        val planetName: String,
        val exploredPaths: List<String>,
        val visitedPoints: List<String>,
        val implicitKnowPoints: List<String>,
        val sentRedirections: List<String>,
        val lastVisitedPoint: String,
        val lastTarget: String,
    ) : RobolabMessage(metadata) {
        override val summary by lazy {
            "${metadata.comTestString}Group dump for $groupId: $planetName with ${exploredPaths.size} explored path(s)"
        }

        override val details by lazy {
            listOf(
                "Group" to groupId,
                "Planet" to planetName,
                "Explored paths" to exploredPaths.joinToString("\n"),
                "Visited points" to visitedPoints.joinToString("\n"),
                "Implicit points" to implicitKnowPoints.joinToString("\n"),
                "Sent redirects" to sentRedirections.joinToString("\n"),
                "Last point" to lastVisitedPoint,
                "Last target" to lastTarget
            )
        }
    }

    class TargetMessage(
        metadata: Metadata,
        val target: PlanetPoint,
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
        val message: String,
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
        val message: String,
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
        val message: String,
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
        val planetName: String,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Testplanet: $planetName" }
        override val details by lazy {
            listOf(
                "Test planet" to planetName
            )
        }
    }

    class ReadyMessage(
        metadata: Metadata,
    ) : RobolabMessage(metadata) {
        override val summary by lazy { "${metadata.comTestString}Ready!" }
        override val details by lazy {
            emptyList<Pair<String, String>>()
        }
    }

    class DebugMessage(
        metadata: Metadata,
        val message: String,
        val firstBluePoint: PlanetPoint?,
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
        val errors: List<String>,
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

    class OdometryMessage(
        metadata: Metadata,
        val odometry: OdometryData,
        val payloadFlags: Flags<OdometryPayloadFlags> = emptyFlags(),
    ) : RobolabMessage(metadata) {
        override val summary: String by lazy {
            "Odometry (${odometry.size})"
        }
        override val details: List<Pair<String, String>> by lazy {
            listOf(
                "Start" to odometry.start.toString(),
                "End" to odometry.end.toString(),
            ) + OdometryPayloadFlags.values().map {
                it.name to (it in payloadFlags).toString()
            }
        }
    }

    class IllegalMessage(
        metadata: Metadata,
        val reason: Reason,
        val errorMessage: String? = null,
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

    override fun toString() = "${
        formatDateTime(
            kotlinx.datetime.Instant.fromEpochMilliseconds(metadata.time),
            "HH:mm:ss"
        )
    }: ${metadata.rawMessage}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RobolabMessage) return false

        return metadata == other.metadata
    }

    override fun hashCode(): Int {
        return metadata.hashCode()
    }

    companion object {
        fun pathToString(path: PlanetPath, from: From) = with(path) {
            "(${source.x},${source.y},${sourceDirection.export()}) → " +
                    "(${target.x},${target.y},${targetDirection.export()})" +
                    (if (blocked) ", blocked" else "") +
                    if (from != From.CLIENT) ", weight: $weight" else ""
        }

        fun pathToDetails(path: PlanetPath, from: From): List<Pair<String, String>> = with(path) {
            val result = mutableListOf(
                "Start" to "${source.x}, ${source.y}, ${sourceDirection.name}",
                "End" to "${target.x}, ${target.y}, ${targetDirection.name}",
                "Status" to if (this.blocked) "blocked" else "free"
            )
            if (from != From.CLIENT) {
                result += "Weight" to weight.toString()
            }

            return result
        }
    }

    val Metadata.comTestString: String
        get() = if (topic.startsWith(Topic.COMTEST.topicName)) "COMTEST: " else ""
}

fun PlanetDirection.export() = when (this) {
    PlanetDirection.North -> "N"
    PlanetDirection.East -> "E"
    PlanetDirection.South -> "S"
    PlanetDirection.West -> "W"
}
