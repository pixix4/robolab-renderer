package de.robolab.renderer.drawable.general

import de.robolab.planet.Comment
import de.robolab.planet.Planet
import de.robolab.renderer.animation.GenericTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext

class CommentAnimatable(
        reference: Comment,
        private val animationTime: IAnimationTime
) : Animatable<Comment>(reference) {

    override val animators = emptyList<GenericTransition<*>>()

    override fun onDraw(context: DrawContext) {
        context.fillText(
                reference.message,
                reference.point,
                context.theme.lineColor,
                alignment = ICanvas.FontAlignment.CENTER
        )
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    override fun startExitAnimation(onFinish: () -> Unit) {
        onFinish()
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        onFinish()
    }

    override fun startUpdateAnimation(obj: Comment, planet: Planet) {
        reference = obj
    }
}
