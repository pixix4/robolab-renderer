package de.robolab.client.theme

import de.robolab.common.utils.Color

object NordDarkTheme : ITheme {


    /**
     * A arctic, north-bluish color palette Java library.
     * Created for the clean- and minimal flat design pattern to achieve a optimal focus and readability for code syntax
     * highlighting and UI.
     * It consists of a total of sixteen, carefully selected, dimmed pastel colors for a eye-comfortable, but yet colorful
     * ambiance.
     *
     *
     * Public API entry point of the [Nord Java](https://github.com/arcticicestudio/nord-java) project, which
     * implements the [Nord](https://github.com/arcticicestudio/nord) color palette.
     *
     * <div>
     * <table summary="Nord Color Palette">
     * <tbody>
     * <tr>
     * <th style="border:0;">Polar Night</th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(46,52,64);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(59,66,82);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(67,76,94);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(76,86,106);margin:0"></th>
    </tr> *
     * <tr>
     * <th style="border:0;">Snow Storm</th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(216,222,233);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(229,233,240);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(236,239,244);margin:0"></th>
    </tr> *
     * <tr>
     * <th style="border:0;">Frost</th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(143,188,187);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(136,192,208);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(129,161,193);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(94,129,172);margin:0"></th>
    </tr> *
     * <tr>
     * <th style="border:0;">Aurora</th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(191,97,106);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(208,135,112);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(235,203,139);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(163,190,140);margin:0"></th>
     * <th style="border:0;width:25px;height:30px;background-color:rgb(180,142,173);margin:0"></th>
    </tr> *
    </tbody> *
    </table> *
    </div> *
     *
     * @author Arctic Ice Studio &lt;development@arcticicestudio.com&gt;
     * @version 0.2.0
     * @since 0.1.0
     */
    private enum class Nord(red: Int, green: Int, blue: Int) {
        /**
         * Base component color "`nord0`" of "Polar Night" with a RGB value of `rgb(46, 52, 64)` and a HEX value
         * of `#2E3440`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(46,52,64);margin:0"></div>
         */
        NORD0(46, 52, 64),

        /**
         * Brighter color "`nord1`" of the "Polar Night" component base color [.NORD0] with a RGB value of
         * `rgb(59, 66, 82)` and a HEX value of `#3B4252`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(59,66,82);margin:0"></div>
         */
        NORD1(59, 66, 82),

        /**
         * Brighter color "`nord2`" of the "Polar Night" component base color [.NORD0] with a RGB value of
         * `rgb(67, 76, 94)` and a HEX value of `#434C5E`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(67,76,94);margin:0"></div>
         */
        NORD2(67, 76, 94),

        /**
         * Brighter color "`nord3`" of the "Polar Night" component base color [.NORD0] with a RGB value of
         * `rgb(76, 86, 106)` and a HEX value of `#4C566A`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(76,86,106);margin:0"></div>
         */
        NORD3(76, 86, 106),

        /**
         * Base component color "`nord4`" of "Snow Storm" with a RGB value of `rgb(216, 222, 233)` and a HEX value
         * of `#D8DEE9`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(216,222,233);margin:0"></div>
         */
        NORD4(216, 222, 233),

        /**
         * Brighter color "`nord5`" of the "Snow Storm" component base color [.NORD4] with a RGB value of
         * `rgb(229, 233, 240)` and a HEX value of `#E5E9F0`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(229,233,240);margin:0"></div>
         */
        NORD5(229, 233, 240),

        /**
         * Brighter color "`nord6`" of the "Snow Storm" component base color [.NORD4] with a RGB value of
         * `rgb(236, 239, 244)` and a HEX value of `#ECEFF4`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(236,239,244);margin:0"></div>
         */
        NORD6(236, 239, 244),

        /**
         * Bluish core color "`nord7`" of the "Frost" component with a RGB value of `rgb(143, 188, 187)` and a
         * HEX value of `#8FBCBB`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(143,188,187);margin:0"></div>
         */
        NORD7(143, 188, 187),

        /**
         * Bluish core accent color "`nord8`" of the "Frost" component with a RGB value of `rgb(136, 192, 208)`
         * and a HEX value of `#88C0D0`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(136,192,208);margin:0"></div>
         */
        NORD8(136, 192, 208),

        /**
         * Bluish core color "`nord9`" of the "Frost" component with a RGB value of `rgb(129, 161, 193)` and a
         * HEX value of `#81A1C1`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(129,161,193);margin:0"></div>
         */
        NORD9(129, 161, 193),

        /**
         * Bluish core color "`nord10`" of the "Frost" component with a RGB value of `rgb(94, 129, 172)` and a
         * HEX value of `#5E81AC`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(94,129,172);margin:0"></div>
         */
        NORD10(94, 129, 172),

        /**
         * Colorful color "`nord11`" of the "Aurora" component with a RGB value of `rgb(191, 97, 106)` and a
         * HEX value of `#BF616A`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(191,97,106);margin:0"></div>
         */
        NORD11(191, 97, 106),

        /**
         * Colorful color "`nord12`" of the "Aurora" component with a RGB value of `rgb(208, 135, 112)` and a
         * HEX value of `#D08770`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(208,135,112);margin:0"></div>
         */
        NORD12(208, 135, 112),

        /**
         * Colorful color "`nord13`" of the "Aurora" component with a RGB value of `rgb(235, 203, 139)` and a
         * HEX value of `#EBCB8B`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(235,203,139);margin:0"></div>
         */
        NORD13(235, 203, 139),

        /**
         * The color `nord14` of the "Aurora" component with an RGB value of `rgb(163, 190, 140)` and an HEX
         * value of `#A3BE8C`.
         * <div style="border:none;width:25px;height:30px;background-color:rgb(163,190,140);margin: 0"></div>
         */
        NORD14(163, 190, 140),

        /**
         * Colorful color "`nord15`" of the "Aurora" component with a RGB value of `rgb(180, 142, 173)` and a
         * HEX value of `#B48EAD`.
         *
         *
         * <div style="border:none;width:25px;height:25px;background-color:rgb(180,142,173);margin:0"></div>
         */
        NORD15(180, 142, 173);

        val color = Color(red, green, blue)
    }


    override val ui = object : IThemeUi {

        override val primaryBackground = Nord.NORD0.color
        override val primaryHoverBackground = Nord.NORD0.color.interpolate(Color.BLACK, 0.06)

        override val secondaryBackground = Nord.NORD1.color
        override val secondaryHoverBackground = Nord.NORD1.color.interpolate(Nord.NORD0.color, 0.4)

        override val tertiaryBackground = Nord.NORD2.color
        override val tertiaryHoverBackground = Nord.NORD2.color.interpolate(Nord.NORD1.color, 0.4)

        override val primaryTextColor = Nord.NORD6.color
        override val secondaryTextColor = Nord.NORD4.color.interpolate(Nord.NORD0.color, 0.4)

        override val themeColor = Nord.NORD8.color
        override val themeHoverColor = Nord.NORD7.color
        override val themePrimaryText = Nord.NORD6.color
        override val themeSecondaryText = Nord.NORD5.color

        override val borderColor = Nord.NORD3.color.interpolate(Nord.NORD4.color, 0.05)

        override val successColor = Nord.NORD14.color
        override val successTextColor = primaryTextColor

        override val warnColor = Nord.NORD13.color
        override val warnTextColor = primaryBackground

        override val errorColor = Nord.NORD11.color
        override val errorTextColor = primaryTextColor
    }

    override val editor = object : IThemeEditor {

        // Blue
        override val editorKeywordColor = Nord.NORD8.color

        // Yellow-Brown
        override val editorDirectionColor = Nord.NORD9.color

        // Yellow-Green
        override val editorNumberColor = Nord.NORD15.color

        // Green
        override val editorCommentColor = Nord.NORD3.color.interpolate(Nord.NORD4.color, 0.2)

        // Orange-Brown
        override val editorStringColor = Nord.NORD14.color

        // Red
        override val editorErrorColor = Nord.NORD13.color

        // Background highlight
        override val editorSelectedLineColor = Nord.NORD1.color
    }

    override val plotter = object : IThemePlotter {

        override val primaryBackgroundColor = ui.primaryBackground
        override val secondaryBackgroundColor = ui.secondaryBackground

        override val lineColor = ui.primaryTextColor

        override val gridColor = secondaryBackgroundColor.interpolate(lineColor, 0.15)
        override val gridTextColor = secondaryBackgroundColor.interpolate(lineColor, 0.4)

        override val redColor = Nord.NORD11.color
        override val blueColor = Nord.NORD10.color

        override val highlightColor = Nord.NORD13.color
        override val editColor = Nord.NORD15.color

        override val robotMainColor = Nord.NORD7.color
        override val robotDisplayColor = primaryBackgroundColor
        override val robotWheelColor = lineColor
        override val robotSensorColor = robotWheelColor.interpolate(robotMainColor, 0.1)
        override val robotButtonColor = lineColor
    }

    override val traverser = LightTheme.traverser
}
