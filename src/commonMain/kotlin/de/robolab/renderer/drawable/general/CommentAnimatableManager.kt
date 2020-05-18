package de.robolab.renderer.drawable.general

import de.robolab.planet.Comment
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.AnimatableManager

class CommentAnimatableManager : AnimatableManager<Comment, CommentAnimatable>() {

    override fun getObjectList(planet: Planet): List<Comment> {
        return planet.commentList
    }

    override fun createAnimatable(obj: Comment, planet: Planet): CommentAnimatable {
        return CommentAnimatable(obj)
    }
}
