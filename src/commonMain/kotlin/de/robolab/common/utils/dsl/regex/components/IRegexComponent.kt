package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.IRegexStyle
import de.robolab.common.utils.dsl.regex.RegexDSLMarker

@RegexDSLMarker
interface IRegexComponent {
    fun toPattern(
        builder: StringBuilder,
        style: IRegexStyle,
        isGrouped: Boolean,
        nestedIn: ComponentWithRegex
    ) : Boolean
}