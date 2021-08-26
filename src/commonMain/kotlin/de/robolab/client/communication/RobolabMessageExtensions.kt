package de.robolab.client.communication

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.*
import de.robolab.common.planet.utils.PlanetVersion


fun List<RobolabMessage>.toServerPlanet(): Pair<Planet, List<PlanetPoint>> {
    var name = ""
    var startPoint = PlanetStartPoint(0L, 0L, PlanetDirection.North, null)
    val pathList = mutableListOf<PlanetPath>()
    val targetList = mutableListOf<PlanetTarget>()
    val pathSelectList = mutableListOf<PlanetPathSelect>()
    val visitedPointList = mutableListOf<PlanetPoint>()

    var currentPoint = PlanetPoint(0, 0)
    var currentDirection = PlanetDirection.North
    for (message in this) {
        when (message) {
            is RobolabMessage.PathSelectMessageFromRobot -> {
                currentDirection = message.direction
            }
        }

        if (message.metadata.from != From.SERVER) continue

        when (message) {
            is RobolabMessage.PathUnveiledMessage -> {
                pathList.removeAll { it.equalPath(message.path) }

                pathList += message.path.copy(
                    exposure = setOf(PlanetPathExposure(currentPoint))
                )
            }
            is RobolabMessage.PathMessage -> {
                var path = message.path

                if (currentPoint == path.source) {
                    if (currentPoint == path.target && currentDirection == path.targetDirection) {
                        // Loop
                        path = path.reversed()
                    }
                } else if (currentPoint == path.target) {
                    path = path.reversed()
                }

                currentPoint = path.target
                currentDirection = path.targetDirection

                pathList.removeAll { it.equalPath(path) }

                pathList += path
                visitedPointList += currentPoint
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                pathSelectList += PlanetPathSelect(currentPoint, message.direction)
                currentDirection = message.direction
            }
            is RobolabMessage.PlanetMessage -> {
                name = message.planetName
                startPoint = PlanetStartPoint(message.startPoint, message.startOrientation, null)
                currentPoint = message.startPoint
                currentDirection = message.startOrientation
                visitedPointList += currentPoint
            }
            is RobolabMessage.TargetMessage -> {
                targetList.clear()
                targetList += PlanetTarget(message.target, setOf(currentPoint))
            }
        }
    }

    return Planet(
        bluePoint = null,
        comments = emptyList(),
        name = name,
        paths = pathList,
        pathSelects = pathSelectList,
        senderGroupings = emptyList(),
        startPoint = startPoint,
        tags = emptyMap(),
        targets = targetList,
        testSuite = null,
        version = PlanetVersion.CURRENT,
    ).generateSenderGroupings() to visitedPointList
}

fun List<RobolabMessage>.toMqttPlanet(): Planet {
    var name = ""
    var startPoint = PlanetStartPoint(0L, 0L, PlanetDirection.North, null)
    val pathList = mutableListOf<PlanetPath>()
    val targetList = mutableListOf<PlanetTarget>()
    val pathSelectList = mutableListOf<PlanetPathSelect>()

    var currentPoint = PlanetPoint(0, 0)
    var currentDirection = PlanetDirection.North
    for (message in this) {
        if (message.metadata.from != From.CLIENT) continue

        when (message) {
            is RobolabMessage.PathMessage -> {
                pathList += message.path

                if (currentPoint == message.path.source) {
                    if (currentPoint == message.path.target && currentDirection != message.path.sourceDirection) {
                        // Loop
                        currentPoint = message.path.source
                        currentDirection = message.path.sourceDirection
                    } else {
                        currentPoint = message.path.target
                        currentDirection = message.path.targetDirection
                    }
                } else {
                    currentPoint = message.path.source
                    currentDirection = message.path.sourceDirection
                }
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
                currentDirection = message.direction
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                pathSelectList += PlanetPathSelect(currentPoint, message.direction)
                currentDirection = message.direction
            }
            is RobolabMessage.PlanetMessage -> {
                name = message.planetName
                startPoint = PlanetStartPoint(message.startPoint, message.startOrientation, null)
                currentPoint = message.startPoint
                currentDirection = message.startOrientation
            }
            is RobolabMessage.TargetMessage -> {
                targetList.clear()
                targetList += PlanetTarget(message.target, setOf(currentPoint))
            }
        }
    }

    return Planet(
        bluePoint = null,
        comments = emptyList(),
        name = name,
        paths = pathList,
        pathSelects = pathSelectList,
        senderGroupings = emptyList(),
        startPoint = startPoint,
        tags = emptyMap(),
        targets = targetList,
        testSuite = null,
        version = PlanetVersion.CURRENT,
    ).generateSenderGroupings()
}

fun List<RobolabMessage>.toRobot(groupNumber: Int?, backwardMotion: Boolean = false): RobotDrawable.Robot? {
    var currentPoint: PlanetPoint? = null
    var currentDirection = PlanetDirection.North
    var beforePoint = true

    loop@ for (message in this) {
        when (message) {
            is RobolabMessage.PathUnveiledMessage -> {

            }
            is RobolabMessage.PathMessage -> {
                currentPoint = message.path.target
                currentDirection = message.path.targetDirection

                beforePoint = true
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
                currentPoint = message.point
                currentDirection = message.direction
                beforePoint = false
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                currentDirection = message.direction
                beforePoint = false
            }
            is RobolabMessage.PlanetMessage -> {
                currentPoint = message.startPoint
                currentDirection = message.startOrientation.opposite()
                beforePoint = true
            }
        }
    }

    return RobotDrawable.Robot(currentPoint ?: return null, currentDirection, beforePoint, groupNumber, backwardMotion)
}
