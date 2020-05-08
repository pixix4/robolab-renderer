package de.robolab.renderer.drawable.general

import de.robolab.planet.Comment
import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.AnimatableManager
import de.robolab.renderer.drawable.base.IAnimationTime

class CommentAnimatableManager(
        private val animationTime: IAnimationTime
) : AnimatableManager<Comment, CommentAnimatable>() {

    override fun getObjectList(planet: Planet): List<Comment> {
        return planet.commentList
    }

    override fun createAnimatable(obj: Comment, planet: Planet): CommentAnimatable {
        return CommentAnimatable(
                obj,
                animationTime
        )
    }
}
