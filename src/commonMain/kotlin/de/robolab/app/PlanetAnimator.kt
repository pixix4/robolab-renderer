package de.robolab.app

import de.robolab.planet.Planet
import de.robolab.renderer.drawable.live.RobotDrawable
import kotlin.random.Random

class PlanetAnimator(private val referencePlanet: Planet) {

    var planet: Planet = Planet.EMPTY.copy(
            name = referencePlanet.name,
            bluePoint = referencePlanet.bluePoint
    )
    var robot: RobotDrawable.Robot? = null

    private var isFinished = false

    fun update() {
        if (isFinished) {
            isFinished = false
            planet = Planet.EMPTY
            robot = null
            return
        }

        val r = robot
        if (r == null) {
            if (referencePlanet.startPoint != null) {
                robot = RobotDrawable.Robot(
                        referencePlanet.startPoint.point,
                        referencePlanet.startPoint.orientation.opposite(),
                        true,
                        Random.nextInt()
                )
                return
            }

            isFinished = true
            return
        }

        if (r.beforePoint) {
            // Select next direction

            val pathListOfPoint = referencePlanet.pathList.filter { it.connectsWith(r.point) }.toMutableList()

            if (pathListOfPoint.isEmpty()) {
                isFinished = true
                return
            }

            val sourcePath = pathListOfPoint.find { it.connectsWith(r.point, r.direction) }

            if (sourcePath != null) {
                if (pathListOfPoint.size > 1) {
                    pathListOfPoint -= sourcePath
                }

                if (planet.pathList.none { it.equalPath(sourcePath) }) {
                    planet = if (sourcePath.source == r.point && sourcePath.sourceDirection == r.direction) {
                        planet.copy(
                                pathList = planet.pathList + sourcePath.reversed()
                        )
                    } else {
                        planet.copy(
                                pathList = planet.pathList + sourcePath
                        )
                    }
                }
            }

            val next = pathListOfPoint.random()

            robot = if (next.source == r.point) {
                RobotDrawable.Robot(
                        r.point,
                        next.sourceDirection,
                        false,
                        r.groupNumber
                )
            } else {
                RobotDrawable.Robot(
                        r.point,
                        next.targetDirection,
                        false,
                        r.groupNumber
                )
            }
        } else {
            // Drive path

            val path = referencePlanet.pathList.find { it.connectsWith(r.point, r.direction) }

            if (path == null) {
                isFinished = true
                return
            }

            robot = if (path.source == r.point && path.sourceDirection == r.direction) {
                RobotDrawable.Robot(
                        path.target,
                        path.targetDirection,
                        true,
                        r.groupNumber
                )
            } else {
                RobotDrawable.Robot(
                        path.source,
                        path.sourceDirection,
                        true,
                        r.groupNumber
                )
            }
        }
    }
}
