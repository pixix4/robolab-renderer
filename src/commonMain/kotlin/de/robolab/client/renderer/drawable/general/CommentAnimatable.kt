package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.TextView
import de.robolab.common.planet.Comment
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Point
import kotlin.math.round

class CommentAnimatable(
    reference: Comment,
    private val editCallback: IEditCallback?
) : Animatable<Comment>(reference) {

    private val Comment.fontAlignment
    get() = when(alignment) {
        Comment.Alignment.LEFT -> ICanvas.FontAlignment.LEFT
        Comment.Alignment.CENTER ->  ICanvas.FontAlignment.CENTER
        Comment.Alignment.RIGHT ->  ICanvas.FontAlignment.RIGHT
    }

    override val view = TextView(
            reference.point,
            12.0,
            reference.lines.joinToString("\n"),
            ViewColor.LINE_COLOR,
            reference.fontAlignment,
            ICanvas.FontWeight.NORMAL
    ) {
        val callback = editCallback ?: return@TextView false

        callback.setCommentValue(this.reference, it.split('\n'))

        true
    }

    override fun onUpdate(obj: Comment, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setSource(reference.point)
        view.text = reference.lines.joinToString("\n")
        view.alignment = reference.fontAlignment
        view.requestRedraw()
    }

    init {
        view.focusable = editCallback != null
        view.animationTime = 0.0

        var groupHistory = false
        view.onPointerDown {
            groupHistory = false
        }
        view.onPointerDrag { event ->
            val callback = editCallback ?: return@onPointerDrag

            val position = Point(
                    round(event.planetPoint.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
                    round(event.planetPoint.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            )

            callback.setCommentPosition(this.reference, position, groupHistory)
            groupHistory = true

            event.stopPropagation()
        }

        view.onPointerSecondaryAction { event ->
            val callback = editCallback ?: return@onPointerSecondaryAction
            val comment = this@CommentAnimatable.reference

            view.menu(event, "Comment") {
                menu("Alignment") {
                    for (alignment in Comment.Alignment.values()) {
                        action(alignment.name.toLowerCase().capitalize(), comment.alignment == alignment) {
                            callback.setCommentAlignment(comment, alignment)
                        }
                    }
                }
                action("Delete") {
                    callback.deleteComment(comment)
                }
            }

            event.stopPropagation()
        }
    }
}
