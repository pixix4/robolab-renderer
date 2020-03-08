package de.robolab.renderer.theme

import de.robolab.renderer.data.Color

interface ITheme {
    val primaryBackgroundColor: Color
    val secondaryBackgroundColor: Color

    val gridColor: Color
    val gridTextColor: Color

    val redColor: Color
    val blueColor: Color
    
    val lineColor: Color
    val highlightColor: Color
    val editColor: Color
}