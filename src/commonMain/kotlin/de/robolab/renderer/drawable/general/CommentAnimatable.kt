package de.robolab.renderer.drawable.general

import de.robolab.planet.Comment
import de.robolab.planet.Planet
import de.robolab.renderer.data.Color
import de.robolab.renderer.document.TextView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.platform.ICanvas

class CommentAnimatable(
        reference: Comment
) : Animatable<Comment>(reference) {

    override val view = TextView(
            reference.point,
            12.0,
            reference.message,
            ViewColor.LINE_COLOR,
            ICanvas.FontAlignment.CENTER,
            ICanvas.FontWeight.NORMAL
    )

    override fun onUpdate(obj: Comment, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setCenter(reference.point)
        view.text = reference.message
    }
}
