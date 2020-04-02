package de.robolab.renderer.theme

import de.robolab.renderer.data.Color

object LightTheme : ITheme {
    override val primaryBackgroundColor = Color(255, 255, 255)
    override val secondaryBackgroundColor = Color(240, 240, 240)

    override val gridColor = Color(200, 200, 200)
    override val gridTextColor = Color(180, 180, 180)

    override val redColor = Color(192, 57, 43)
    override val blueColor = Color(41, 128, 185)

    override val lineColor = Color(20, 20, 20)
    override val highlightColor = Color(243, 156, 18)
    override val editColor = Color(142, 68, 173)

    override val robotMainColor = Color(243, 156, 18)
    override val robotDisplayColor = Color(240, 240, 240)
    override val robotWheelColor = lineColor
    override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
    override val robotButtonColor = Color(49, 31, 4)
}
