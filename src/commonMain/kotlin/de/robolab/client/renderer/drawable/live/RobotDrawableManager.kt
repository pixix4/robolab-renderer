package de.robolab.client.renderer.drawable.live

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.common.planet.Planet

class RobotDrawableManager : AnimatableManager<RobotDrawable.Robot?, RobotDrawable>() {

    var robotList = emptyList<RobotDrawable.Robot>()

    override fun getObjectList(planet: Planet): List<RobotDrawable.Robot> {
        return robotList
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
