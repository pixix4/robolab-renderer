package de.robolab.common.net

enum class MIMESuperType(typeKey:String){
    Application("application"),
    Audio("audio"),
    Font("font"),
    Example("example"),
    Image("image"),
    Message("message"),
    Model("model"),
    Multipart("multipart"),
    Text("text"),
    Video("video");

    val typeKey:String = typeKey.lowercase()
    val subTypes: List<MIMEType> by lazy { MIMEType.lowercaseLookup.filterKeys { it.startsWith("${this.typeKey}/") }.values.toList() }

    companion object{
        val lowercaseLookup: Map<String, MIMESuperType> = values().associateBy(MIMESuperType::typeKey)
    }
}

enum class MIMEType(primaryName: String, vararg alsoKnownAs:String) {
    PlainText("text/plain"),
    HTML("text/html"),
    OCTET_STREAM("application/octet-stream"),
    JSON("application/json"),
    JWT("application/jwt"),
    ;

    val primaryName = primaryName.lowercase()
    val knownAs: List<String> = listOf(this.primaryName) + alsoKnownAs.map(String::lowercase)

    companion object{
        val lowercaseLookup: Map<String, MIMEType> = values().flatMap { type-> type.knownAs.map { value->Pair(value, type) } }.toMap()

        fun parse(value: String): MIMEType? = lowercaseLookup[value]
    }

}