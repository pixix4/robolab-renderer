package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.TextView
import de.robolab.common.planet.Comment
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import kotlin.math.round

class CommentAnimatable(
    reference: Comment,
    private val editProperty: ObservableValue<IEditCallback?>
) : Animatable<Comment>(reference) {

    override val view = TextView(
            reference.point,
            12.0,
            reference.message,
            ViewColor.LINE_COLOR,
            ICanvas.FontAlignment.CENTER,
            ICanvas.FontWeight.NORMAL
    ) {
        val callback = editProperty.value ?: return@TextView false

        callback.setCommentValue(this.reference, it)

        true
    }

    override fun onUpdate(obj: Comment, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setCenter(reference.point)
        view.text = reference.message
    }

    init {
        view.focusable = editProperty.value != null
        editProperty.onChange {
            view.focusable = editProperty.value != null
        }
        view.animationTime = 0.0

        var groupHistory = false
        view.onPointerDown {
            groupHistory = false
        }
        view.onPointerDrag { event ->
            val callback = editProperty.value ?: return@onPointerDrag

            val position = Point(
                    round(event.planetPoint.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
                    round(event.planetPoint.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            )

            callback.setCommentPosition(this.reference, position, groupHistory)
            groupHistory = true

            event.stopPropagation()
        }

        view.onPointerSecondaryAction { event ->
            val callback = editProperty.value ?: return@onPointerSecondaryAction

            view.menu(event, "Comment") {
                action("Delete") {
                    callback.deleteComment(this@CommentAnimatable.reference)
                }
            }

            event.stopPropagation()
        }
    }
}
