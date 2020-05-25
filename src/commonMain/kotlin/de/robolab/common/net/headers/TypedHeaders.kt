package de.robolab.common.net.headers

data class TypedHeaders(
    val contentTypeHeaders: List<ContentTypeHeader> = emptyList()
){
    companion object{
        fun parse(headers: Map<String,List<String>>): TypedHeaders{
            return TypedHeaders(
                contentTypeHeaders = headers[ContentTypeHeader.name]?.map(::ContentTypeHeader).orEmpty()
            )
        }
    }
}
