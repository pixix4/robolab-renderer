package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.common.planet.Comment
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class CommentAnimatableManager(
        private val editProperty: ObservableValue<IEditCallback?> = constObservable(null)
) : AnimatableManager<Comment, CommentAnimatable>() {

    override fun objectEquals(oldValue: Comment, newValue: Comment): Boolean {
        if (oldValue == newValue) return true

        if (objectList.contains(newValue)) {
            return false
        }

        return oldValue.lines == newValue.lines || oldValue.point == newValue.point
    }

    override fun getObjectList(planet: Planet): List<Comment> {
        return planet.commentList
    }

    override fun createAnimatable(obj: Comment, planet: Planet): CommentAnimatable {
        return CommentAnimatable(obj, editProperty)
    }
}
