package de.robolab.client.app.model.base

import de.robolab.common.planet.TagQuery

data class SearchRequest(val rawText: String, val tagQueries: List<TagQuery>, val literalQueries: List<String>) {

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