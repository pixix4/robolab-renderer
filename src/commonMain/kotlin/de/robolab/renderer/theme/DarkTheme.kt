package de.robolab.renderer.theme

import de.robolab.renderer.data.Color

object DarkTheme : ITheme {
    override val secondaryBackgroundColor = Color(18, 18, 18)
    override val primaryBackgroundColor = secondaryBackgroundColor.interpolate(de.robolab.renderer.data.Color.WHITE, 0.07)

    override val gridColor = secondaryBackgroundColor.interpolate(Color.WHITE, 0.15)
    override val gridTextColor = secondaryBackgroundColor.interpolate(Color.WHITE, 0.4)

    override val redColor = Color(192, 57, 43)
    override val blueColor = Color(41, 128, 185)

    override val lineColor = Color(255, 255, 255).interpolate(secondaryBackgroundColor, 0.1)
    override val highlightColor = Color(243, 156, 18).interpolate(secondaryBackgroundColor, 0.4)
    override val editColor = Color(46, 204, 113)

    override val robotMainColor = Color(243, 156, 18)
    override val robotDisplayColor = Color(240, 240, 240)
    override val robotWheelColor = lineColor
    override val robotSensorColor = robotWheelColor.interpolate(LightTheme.robotMainColor, 0.1)
    override val robotButtonColor = Color(49, 31, 4)
}
