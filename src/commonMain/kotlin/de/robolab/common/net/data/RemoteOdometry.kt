package de.robolab.common.net.data

class OdometryData(private val data: Array<PositionXYA>) {
    data class PositionXY(val x: Float, val y: Float) {
        override fun toString(): String = "(X:$x; Y:$y)"
    }

    data class PositionXYA(val x: Float, val y: Float, val angle: Float) {
        override fun toString(): String = "(X:$x; Y:$y; γ:$angle)"
    }

    val positionsXY: List<PositionXY>
        get() = data.map { PositionXY(it.x, it.y) }

    val positionsXYA: List<PositionXYA>
        get() = data.toList()

    val size: Int
        get() = data.size

    val start: PositionXYA
        get() = data.first()

    val end: PositionXYA
        get() = data.last()

    fun offset(x: Float, y: Float, angle: Float = 0f): OdometryData {
        return OdometryData(data.map {
            PositionXYA(
                it.x + x,
                it.y + y,
                it.angle + angle
            )
        }.toTypedArray())
    }

    fun scale(x: Float, y: Float, angle: Float = 1f): OdometryData {
        return OdometryData(data.map { PositionXYA(it.x * x, it.y * y, it.angle * angle) }.toTypedArray())
    }

    fun offsetScale(
        offsetX: Float,
        scaleX: Float,
        offsetY: Float,
        scaleY: Float,
        offsetAngle: Float = 0f,
        scaleAngle: Float = 1f
    ): OdometryData {
        return OdometryData(data.map {
            PositionXYA(
                (it.x + offsetX) * scaleX,
                (it.y + offsetY) * scaleY,
                (it.angle + offsetAngle) * scaleAngle
            )
        }.toTypedArray())
    }

    fun scaleOffset(
        scaleX: Float,
        offsetX: Float,
        scaleY: Float,
        offsetY: Float,
        scaleAngle: Float = 1f,
        offsetAngle: Float = 0f
    ): OdometryData {
        return OdometryData(data.map {
            PositionXYA(
                (it.x * scaleX) + offsetX,
                (it.y * scaleY) + offsetY,
                (it.angle * scaleAngle) + offsetAngle
            )
        }.toTypedArray())
    }
}

enum class OdometryPayloadFlags {
    BASE64, //0
    POSITION_GRID_UNITS, //1
    ANGLE_DEGREES, //2
}