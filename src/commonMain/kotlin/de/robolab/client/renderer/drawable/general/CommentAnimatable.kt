package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.TextView
import de.robolab.common.planet.PlanetComment
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetCommentAlignment
import de.robolab.common.utils.Vector
import kotlin.math.round

class CommentAnimatable(
    reference: PlanetComment,
    private val editCallback: IEditCallback?
) : Animatable<PlanetComment>(reference) {

    private val PlanetComment.fontAlignment
    get() = when(alignment) {
        PlanetCommentAlignment.Left -> ICanvas.FontAlignment.LEFT
        PlanetCommentAlignment.Center ->  ICanvas.FontAlignment.CENTER
        PlanetCommentAlignment.Right ->  ICanvas.FontAlignment.RIGHT
    }

    override val view = TextView(
            reference.coordinate.point,
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

    override fun onUpdate(obj: PlanetComment, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setSource(reference.coordinate.point)
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

        view.registerPointerHint(
            "Move comment",
            PointerEvent.Type.DRAG
        ) {
            editCallback != null
        }
        view.onPointerDrag { event ->
            val callback = editCallback ?: return@onPointerDrag

            val position = Vector(
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
                    for (alignment in PlanetCommentAlignment.values()) {
                        action(alignment.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, comment.alignment == alignment) {
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
