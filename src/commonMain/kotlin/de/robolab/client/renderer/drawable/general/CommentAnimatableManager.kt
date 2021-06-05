package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.common.planet.PlanetComment
import de.robolab.common.planet.Planet

class CommentAnimatableManager(
        private val editCallback: IEditCallback? = null
) : AnimatableManager<PlanetComment, CommentAnimatable>() {

    override fun objectEquals(oldValue: PlanetComment, newValue: PlanetComment): Boolean {
        if (oldValue == newValue) return true

        if (objectList.contains(newValue)) {
            return false
        }

        return oldValue.lines == newValue.lines || oldValue.coordinate.point == newValue.coordinate.point
    }

    override fun getObjectList(planet: Planet): List<PlanetComment> {
        return planet.comments
    }

    override fun createAnimatable(obj: PlanetComment, planet: Planet): CommentAnimatable {
        return CommentAnimatable(obj, editCallback)
    }
}
