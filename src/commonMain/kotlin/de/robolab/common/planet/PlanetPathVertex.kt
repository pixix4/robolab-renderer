package de.robolab.common.planet

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import kotlin.reflect.KClass

data class PlanetPathVertex(val point: PlanetPoint, val direction: PlanetDirection) : IReplCommandParameter {
    fun toShortString(): String = "${point.x},${point.y},${direction.letter}"


    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion
    override fun toToken(): String = "${point.x},${point.y},${direction.letter}"


    companion object : IReplCommandParameterTypeDescriptor<PlanetPathVertex> {
        override val klazz: KClass<PlanetPathVertex> = PlanetPathVertex::class
        override val description: String = "The \"side\" of a point - coordinates combined with a direction"
        override val example: List<String> = listOf("5,6,E")
        override val name: String = "PathVertex"
        override val pattern: String = "<x>,<y>,<d>"
        override val regex: Regex = "^(-?\\d+),(-?\\d+),([NESW])$".toRegex()
        override fun fromToken(token: String, match: MatchResult): PlanetPathVertex = PlanetPathVertex(
            PlanetPoint(
                match.groups[1]!!.value.toLong(),
                match.groups[2]!!.value.toLong()
            ),
            PlanetDirection.fromLetter(match.groups[3]!!.value[0])!!
        )
    }
}
