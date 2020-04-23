package de.robolab.communication

import de.robolab.planet.*
import de.robolab.renderer.drawable.live.RobotDrawable


fun List<RobolabMessage>.toServerPlanet(): Planet {
    var name = ""
    var startPoint: StartPoint? = null
    val pathList = mutableListOf<Path>()
    val targetList = mutableListOf<TargetPoint>()
    val pathSelectList = mutableListOf<PathSelect>()

    var currentPoint = Coordinate(0, 0)
    var currentDirection = Direction.NORTH
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
                        exposure = setOf(currentPoint)
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
            }
            is RobolabMessage.PathSelectMessageFromServer -> {
                pathSelectList += PathSelect(currentPoint, message.direction)
                currentDirection = message.direction
            }
            is RobolabMessage.PlanetMessage -> {
                name = message.planetName
                startPoint = StartPoint(message.startPoint, message.startOrientation, emptyList())
                currentPoint = message.startPoint
                currentDirection = message.startOrientation
            }
            is RobolabMessage.TargetMessage -> {
                targetList.clear()
                targetList += TargetPoint(message.target, currentPoint)
            }
        }
    }

    return Planet(
            name,
            startPoint,
            null,
            pathList,
            targetList,
            pathSelectList,
            emptyList()
    )
}

fun List<RobolabMessage>.toMqttPlanet(): Planet {
    var name = ""
    var startPoint: StartPoint? = null
    var bluePoint: Coordinate? = null
    val pathList = mutableListOf<Path>()
    val targetList = mutableListOf<TargetPoint>()
    val pathSelectList = mutableListOf<PathSelect>()

    var currentPoint = Coordinate(0, 0)
    var currentDirection = Direction.NORTH
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
                pathSelectList += PathSelect(currentPoint, message.direction)
                currentDirection = message.direction
            }
            is RobolabMessage.PlanetMessage -> {
                name = message.planetName
                startPoint = StartPoint(message.startPoint, message.startOrientation, emptyList())
                currentPoint = message.startPoint
                currentDirection = message.startOrientation
            }
            is RobolabMessage.SetPlanetMessage -> {

            }
            is RobolabMessage.TargetMessage -> {
                targetList.clear()
                targetList += TargetPoint(message.target, currentPoint)
            }
            is RobolabMessage.TargetReachedMessage -> {

            }
            is RobolabMessage.ExplorationCompletedMessage -> {

            }
            is RobolabMessage.DoneMessage -> {

            }
            is RobolabMessage.TestplanetMessage -> {

            }
            is RobolabMessage.ReadyMessage -> {

            }
            is RobolabMessage.DebugMessage -> {

            }
            is RobolabMessage.IllegalMessage -> {

            }
        }
    }

    return Planet(
            name,
            startPoint,
            bluePoint,
            pathList,
            targetList,
            pathSelectList,
            emptyList()
    )
}

fun List<RobolabMessage>.toRobot(groupNumber: Int?): RobotDrawable.Robot? {
    var currentPoint = Coordinate(0, 0)
    var currentDirection = Direction.NORTH
    var beforePoint = true
    loop@ for (message in this) {

        when (message) {
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

                beforePoint = true
            }
            is RobolabMessage.PathSelectMessageFromRobot -> {
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

    return RobotDrawable.Robot(currentPoint, currentDirection, beforePoint, groupNumber)
}
