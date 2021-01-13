
package de.robolab.client.renderer.utils

import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point

@Suppress("unused")
class LoggedTransformation(
    private val transformation: ITransformation
) : ITransformation by transformation {

    private val logger = Logger("LoggedTransformation")

    override fun translateBy(point: Point, duration: Double) {
        logger.info {
            "translateBy(point: $point, duration: $duration)"
        }
        transformation.translateBy(point, duration)
    }

    override fun translateTo(point: Point, duration: Double) {
        logger.info {
            "translateTo(point: $point, duration: $duration)"
        }
        transformation.translateTo(point, duration)
    }

    override fun setTranslation(point: Point) {
        logger.info {
            "setTranslation(point: $point)"
        }
        transformation.setTranslation(point)
    }

    override fun rotateBy(angle: Double, center: Point, duration: Double) {
        logger.info {
            "rotateBy(angle: $angle, center: $center, duration: $duration)"
        }
        transformation.rotateBy(angle, center, duration)
    }

    override fun rotateTo(angle: Double, center: Point, duration: Double) {
        logger.info {
            "rotateTo(angle: $angle, center: $center, duration: $duration)"
        }
        transformation.rotateTo(angle, center, duration)
    }

    override fun setRotationAngle(angle: Double) {
        logger.info {
            "setRotationAngle(angle: $angle)"
        }
        transformation.setRotationAngle(angle)
    }

    override fun scaleBy(factor: Double, center: Point, duration: Double) {
        logger.info {
            "scaleBy(factor: $factor, center: $center, duration: $duration)"
        }
        transformation.scaleBy(factor, center, duration)
    }

    override fun scaleTo(scale: Double, center: Point, duration: Double) {
        logger.info {
            "scaleTo(scale: $scale, center: $center, duration: $duration)"
        }
        transformation.scaleTo(scale, center, duration)
    }

    override fun scaleIn(center: Point, duration: Double) {
        logger.info {
            "scaleIn(center: $center, duration: $duration)"
        }
        transformation.scaleIn(center, duration)
    }

    override fun scaleOut(center: Point, duration: Double) {
        logger.info {
            "scaleOut(center: $center, duration: $duration)"
        }
        transformation.scaleOut(center, duration)
    }

    override fun resetScale(center: Point, duration: Double) {
        logger.info {
            "resetScale(center: $center, duration: $duration)"
        }
        transformation.resetScale(center, duration)
    }

    override fun setScaleFactor(scale: Double) {
        logger.info {
            "setScaleFactor(scale: $scale)"
        }
        transformation.setScaleFactor(scale)
    }

    override fun flip(force: Boolean?) {
        logger.info {
            "flip(force: $force)"
        }
        transformation.flip(force)
    }
}
