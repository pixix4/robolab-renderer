package de.robolab.communication

import de.robolab.model.*
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.live.RobotDrawable


fun List<RobolabMessage>.toServerPlanet(maximumIndex: Int? = null): Planet {
    val messageList = if (maximumIndex != null) this.subList(0, maximumIndex + 1) else this
    val (name, startPoint, startOrientation) =
            (firstOrNull { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage)?.let {
                Triple(it.planetName, it.startPoint, it.startOrientation)
            }
                    ?: Triple(firstOrNull { it.metadata.topic.startsWith("planet/") }?.metadata?.topic?.substringAfter('/')?.substringBeforeLast("-"), null, Direction.NORTH)
    val bluePointMessage = this.filterIsInstance<RobolabMessage.DebugMessage>().find { it.firstBluePoint != null }
    val pathSelects = mutableListOf<PathSelect>()
    var lastSelectPathFromRobot: RobolabMessage.PathSelectMessageFromRobot? = null

    for (message in messageList) {
        when (message) {
            is RobolabMessage.PathSelectMessageFromRobot -> {
                lastSelectPathFromRobot = message
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                if (lastSelectPathFromRobot != null) {
                    pathSelects.add(PathSelect(lastSelectPathFromRobot.point, message.direction))
                }
                lastSelectPathFromRobot = null
            }
            else -> lastSelectPathFromRobot = null
        }
    }

    var lastPoint: Coordinate? = null

    var robotPosition: Coordinate? = null
    var robotDirection: Direction? = null
    var robotServerDirection: Direction? = null
    var robotPathSelect: Direction? = null
    var robotServerPathSelect: Direction? = null
    var target: TargetPoint? = null

    val paths = mutableListOf<Path>()
    for (message in messageList) {
        when (message) {
            is RobolabMessage.PlanetMessage -> {
                robotPosition = message.startPoint
                robotServerDirection = Direction.SOUTH
            }
            is RobolabMessage.FoundPathMessage -> {
                paths += message.path
                if (robotPosition != message.path.target) {
                    robotPosition = message.path.target
                    robotDirection = null
                    robotServerDirection = null
                    robotPathSelect = null
                    robotServerPathSelect = null
                }
                if (message.metadata.from == From.SERVER) {
                    lastPoint = message.path.target
                    robotServerDirection = message.path.targetDirection
                } else {
                    lastPoint = null
                    robotDirection = message.path.targetDirection
                }
            }
            is RobolabMessage.PathUnveiledMessage -> {
                lastPoint?.let {
                    paths += message.path.copy(exposure = setOf(it))
                } ?: paths.add(message.path)
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
                robotPathSelect = message.direction
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                robotServerPathSelect = message.direction
            }
            is RobolabMessage.TargetMessage -> {
                target = TargetPoint(message.target, lastPoint ?: Coordinate(0, 0))
            }
        }
    }

    val start = startPoint?.let { StartPoint(it, startOrientation, emptyList()) }
    val targets = listOfNotNull(target)
    return Planet(
            name ?: "",
            start,
            bluePointMessage?.firstBluePoint,
            paths,
            targets,
            pathSelects
    )
}

fun List<RobolabMessage>.toMqttPlanet(maximumIndex: Int? = null): Planet {
    val messageList = if (maximumIndex != null) this.subList(0, maximumIndex + 1) else this
    val (name, startPoint, startOrientation) =
            (firstOrNull { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage)?.let {
                Triple(it.planetName, it.startPoint, it.startOrientation)
            }
                    ?: Triple(firstOrNull { it.metadata.topic.startsWith("planet/") }?.metadata?.topic?.substringAfter('/')?.substringBeforeLast("-"), null, Direction.NORTH)
    val bluePointMessage = this.filterIsInstance<RobolabMessage.DebugMessage>().find { it.firstBluePoint != null }
    val pathSelects = mutableListOf<PathSelect>()
    var lastSelectPathFromRobot: RobolabMessage.PathSelectMessageFromRobot? = null

    for (message in messageList) {
        when (message) {
            is RobolabMessage.PathSelectMessageFromRobot -> {
                lastSelectPathFromRobot = message
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                if (lastSelectPathFromRobot != null) {
                    pathSelects.add(PathSelect(lastSelectPathFromRobot.point, message.direction))
                }
                lastSelectPathFromRobot = null
            }
            else -> lastSelectPathFromRobot = null
        }
    }

    var lastPoint: Coordinate? = null

    var robotPosition: Coordinate? = null
    var robotDirection: Direction? = null
    var robotServerDirection: Direction? = null
    var robotPathSelect: Direction? = null
    var robotServerPathSelect: Direction? = null
    var target: TargetPoint? = null

    val paths = mutableListOf<Path>()
    for (message in messageList) {
        when (message) {
            is RobolabMessage.PlanetMessage -> {
                robotPosition = message.startPoint
                robotServerDirection = Direction.SOUTH
            }
            is RobolabMessage.FoundPathMessage -> {
                paths += message.path
                if (robotPosition != message.path.target) {
                    robotPosition = message.path.target
                    robotDirection = null
                    robotServerDirection = null
                    robotPathSelect = null
                    robotServerPathSelect = null
                }
                if (message.metadata.from == From.SERVER) {
                    lastPoint = message.path.target
                    robotServerDirection = message.path.targetDirection
                } else {
                    lastPoint = null
                    robotDirection = message.path.targetDirection
                }
            }
            is RobolabMessage.PathUnveiledMessage -> {
                lastPoint?.let {
                    paths += message.path.copy(exposure = setOf(it))
                } ?: paths.add(message.path)
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
                robotPathSelect = message.direction
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                robotServerPathSelect = message.direction
            }
            is RobolabMessage.TargetMessage -> {
                target = TargetPoint(message.target, lastPoint ?: Coordinate(0, 0))
            }
        }
    }

    val start = startPoint?.let { StartPoint(it, startOrientation, emptyList()) }
    val targets = listOfNotNull(target)
    return Planet(
            name ?: "",
            start,
            bluePointMessage?.firstBluePoint,
            paths,
            targets,
            pathSelects
    )
}

fun List<RobolabMessage>.toRobot(groupNumber: Int?, maximumIndex: Int? = null): RobotDrawable.Robot? {
    val messageList = if (maximumIndex != null) this.subList(0, maximumIndex + 1) else this
    val (name, startPoint, startOrientation) =
            (firstOrNull { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage)?.let {
                Triple(it.planetName, it.startPoint, it.startOrientation)
            }
                    ?: Triple(firstOrNull { it.metadata.topic.startsWith("planet/") }?.metadata?.topic?.substringAfter('/')?.substringBeforeLast("-"), null, Direction.NORTH)
    val bluePointMessage = this.filterIsInstance<RobolabMessage.DebugMessage>().find { it.firstBluePoint != null }
    val pathSelects = mutableListOf<PathSelect>()
    var lastSelectPathFromRobot: RobolabMessage.PathSelectMessageFromRobot? = null

    for (message in messageList) {
        when (message) {
            is RobolabMessage.PathSelectMessageFromRobot -> {
                lastSelectPathFromRobot = message
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                if (lastSelectPathFromRobot != null) {
                    pathSelects.add(PathSelect(lastSelectPathFromRobot.point, message.direction))
                }
                lastSelectPathFromRobot = null
            }
            else -> lastSelectPathFromRobot = null
        }
    }

    var lastPoint: Coordinate? = null

    var robotPosition: Coordinate? = null
    var robotDirection: Direction? = null
    var robotServerDirection: Direction? = null
    var robotPathSelect: Direction? = null
    var robotServerPathSelect: Direction? = null
    var target: TargetPoint? = null

    val paths = mutableListOf<Path>()
    for (message in messageList) {
        when (message) {
            is RobolabMessage.PlanetMessage -> {
                robotPosition = message.startPoint
                robotServerDirection = Direction.SOUTH
            }
            is RobolabMessage.FoundPathMessage -> {
                paths += message.path
                if (robotPosition != message.path.target) {
                    robotPosition = message.path.target
                    robotDirection = null
                    robotServerDirection = null
                    robotPathSelect = null
                    robotServerPathSelect = null
                }
                if (message.metadata.from == From.SERVER) {
                    lastPoint = message.path.target
                    robotServerDirection = message.path.targetDirection
                } else {
                    lastPoint = null
                    robotDirection = message.path.targetDirection
                }
            }
            is RobolabMessage.PathUnveiledMessage -> {
                lastPoint?.let {
                    paths += message.path.copy(exposure = setOf(it))
                } ?: paths.add(message.path)
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
                robotPathSelect = message.direction
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                robotServerPathSelect = message.direction
            }
            is RobolabMessage.TargetMessage -> {
                target = TargetPoint(message.target, lastPoint ?: Coordinate(0, 0))
            }
        }
    }

    val robots = mutableListOf<RobotDrawable.Robot>()
    if (robotPosition != null) {
        if (robotPathSelect != null) {
            robots += RobotDrawable.Robot(robotPosition, robotPathSelect, true, groupNumber)
        } else if (robotDirection != null) {
            robots += RobotDrawable.Robot(robotPosition, robotDirection, false, groupNumber)
        }
        if (robotServerPathSelect != null) {
            robots += RobotDrawable.Robot(robotPosition, robotServerPathSelect, true, groupNumber)
        } else if (robotServerDirection != null) {
            robots += RobotDrawable.Robot(robotPosition, robotServerDirection, false, groupNumber)
        }
    }

    return robots.lastOrNull()
}
