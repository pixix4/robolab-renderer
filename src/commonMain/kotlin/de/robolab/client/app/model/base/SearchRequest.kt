package de.robolab.client.app.model.base

import de.robolab.common.planet.utils.IPlanetInfo
import de.robolab.common.planet.Planet
import de.robolab.common.planet.utils.TagQuery

data class SearchRequest(val rawText: String, val tagQueries: List<TagQuery>, val literalQueries: List<String>) {

    fun matches(planetInfo: IPlanetInfo<*>, ignoreCase: Boolean = true): Boolean{
        return literalQueries.all{
            planetInfo.name.contains(it, ignoreCase) || planetInfo.tags.containsKey(it)
        } && tagQueries.all {
            val tagEntry: List<String> = planetInfo.tags[it.tagName] ?: return@all it.matchMissing
            return@all it.matches(tagEntry)
        }
    }

    fun matches(planet: Planet, ignoreCase: Boolean = true): Boolean{
        return literalQueries.all{
            planet.name.contains(it, ignoreCase) || planet.tags.containsKey(it)
        } && tagQueries.all {
            val tagEntry: List<String> = planet.tags[it.tagName] ?: return@all it.matchMissing
            return@all it.matches(tagEntry)
        }
    }

    companion object {

        val REGEX: Regex = """(\$?)(\w[\w-]*)(?:([:<>=])\s?(\w[\w-]*))?""".toRegex()
        //val EmptyRequest = SearchRequest("", emptyList(), emptyList())
        fun parse(text: String): SearchRequest {
            val matches = REGEX.findAll(text).toList()
            var unparsedText = text
            val tagQueries: MutableList<TagQuery> = mutableListOf()
            val literalQueries: MutableList<String> = mutableListOf()
            for (match in matches.reversed()) {
                unparsedText = unparsedText.removeRange(match.range)
                val tagName: String = match.groupValues[2]
                val operator: String? = match.groupValues.getOrNull(3)
                val argument: String? = match.groupValues.getOrNull(4)
                if ((!operator.isNullOrBlank()) && (!argument.isNullOrBlank())) {
                    tagQueries.add(TagQuery.fromOperator(tagName, operator, argument))
                } else if(match.groupValues.getOrNull(1) == "$") {
                    tagQueries.add(TagQuery.HasTag(tagName))
                } else {
                    literalQueries.add(tagName)
                }
            }
            literalQueries.addAll(unparsedText.split(' ').filter(String::isNotEmpty))
            return SearchRequest(text, tagQueries, literalQueries)
        }
    }
}


infix fun IPlanetInfo<*>.matches(request: SearchRequest):Boolean = request.matches(this)
infix fun Planet.matches(request: SearchRequest):Boolean = request.matches(this)