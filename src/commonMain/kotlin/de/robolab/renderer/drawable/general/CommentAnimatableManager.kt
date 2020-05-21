package de.robolab.renderer.drawable.general

import de.robolab.planet.Comment
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.edit.IEditCallback
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

        return oldValue.message == newValue.message || oldValue.point == newValue.point
    }

    override fun getObjectList(planet: Planet): List<Comment> {
        return planet.commentList
    }

    override fun createAnimatable(obj: Comment, planet: Planet): CommentAnimatable {
        return CommentAnimatable(obj, editProperty)
    }
}
