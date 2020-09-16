package de.robolab.client.renderer.drawable.live

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.common.planet.Planet

class RobotDrawableManager : AnimatableManager<RobotDrawable.Robot?, RobotDrawable>() {

    private var robotsChanged = false
    var robotList = emptyList<RobotDrawable.Robot>()
        set(value) {
            robotsChanged = true
            field = value
        }

    override fun getObjectList(planet: Planet): List<RobotDrawable.Robot> {
        return robotList
    }

    override fun forceUpdate(oldPlanet: Planet, newPlanet: Planet): Boolean {
        if (robotsChanged) {
            robotsChanged = false
            return true
        }
        return false
    }

    override fun objectEquals(oldValue: RobotDrawable.Robot?, newValue: RobotDrawable.Robot?): Boolean {
        if (oldValue != null && newValue != null) {
            return oldValue.groupNumber == newValue.groupNumber
        }
        return oldValue == newValue
    }

    override fun createAnimatable(obj: RobotDrawable.Robot?, planet: Planet): RobotDrawable {
        return RobotDrawable(obj)
    }
}
