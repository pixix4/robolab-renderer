package de.robolab.server.externaljs.mongoose

import de.robolab.server.externaljs.*
import de.robolab.server.jsutils.isUndefined
import kotlin.js.Promise

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
val mongoose: Mongoose = js("require(\"mongoose\")") as Mongoose

fun Mongoose.connectOptions(
    uri: String,
    dbName: String? = undefined,
    poolSize: Int? = undefined,
    useUnifiedTopology: Boolean = true,
    serverSelectionTimeoutMS: Int? = undefined,
    heartbeatFrequencyMS: Int? = undefined,
    autoIndex: Boolean? = undefined,
    useNewUrlParser: Boolean = true,
    useCreateIndex: Boolean? = undefined,
    useFindAndModify: Boolean? = undefined,
    reconnectTries: Int? = undefined,
    reconnectInterval: Int? = undefined,
    bufferMaxEntries: Int? = undefined,
    connectTimeoutMS: Int? = undefined,
    socketTimeoutMS: Int? = undefined,
    family: Int? = undefined
): Promise<Mongoose> = this.connect(uri, dynamicAlso {
    if (!dbName.isUndefined()) it.dbName = dbName
    if (!poolSize.isUndefined()) it.poolSize = poolSize
    it.useUnifiedTopology = useUnifiedTopology
    if (!serverSelectionTimeoutMS.isUndefined()) it.serverSelectionTimeoutMS = serverSelectionTimeoutMS
    if (!heartbeatFrequencyMS.isUndefined()) it.heartbeatFrequencyMS = heartbeatFrequencyMS
    if (!autoIndex.isUndefined()) it.autoIndex = autoIndex
    it.useNewUrlParser = useNewUrlParser
    if (!useCreateIndex.isUndefined()) it.useCreateIndex = useCreateIndex
    if (!useFindAndModify.isUndefined()) it.useFindAndModify = useFindAndModify
    if (!reconnectTries.isUndefined()) it.reconnectTries = reconnectTries
    if (!reconnectInterval.isUndefined()) it.reconnectInterval = reconnectInterval
    if (!bufferMaxEntries.isUndefined()) it.bufferMaxEntries = bufferMaxEntries
    if (!connectTimeoutMS.isUndefined()) it.connectTimeoutMS = connectTimeoutMS
    if (!socketTimeoutMS.isUndefined()) it.socketTimeoutMS = socketTimeoutMS
    if (!family.isUndefined()) it.family = family
})

interface SchemaOptions {
    val autoIndex: Boolean?
    val autoCreate: Boolean?
    val bufferCommands: Boolean?
    val capped: Boolean?
    val collection: String?
    val id: Boolean?
    val _id: Boolean?
    val minimize: Boolean?
    val read: String?
    val writeConcern: dynamic
    val shardKey: dynamic
    val strict: Boolean?
    val strictQuery: Boolean?
    val toJSON: dynamic
    val toObject: dynamic
    val typeKey: String?
    val typePojoToMixed: Boolean?
    val useNestedStrict: Boolean?
    val validateBeforeSave: Boolean?
    val versionKey: String?
    val collation: dynamic
    val selectPopulatedPaths: Boolean?
    val skipVersioning: dynamic
    val timestamps: Boolean?
    val storeSubdocValidationError: Boolean?
}

fun schemaOptions(
    autoIndex: Boolean? = undefined,
    autoCreate: Boolean? = undefined,
    bufferCommands: Boolean? = undefined,
    capped: Boolean? = undefined,
    collection: String? = undefined,
    id: Boolean? = undefined,
    _id: Boolean? = undefined,
    minimize: Boolean? = undefined,
    read: String? = undefined,
    writeConcern: dynamic = undefined,
    shardKey: dynamic = undefined,
    strict: Boolean? = undefined,
    strictQuery: Boolean? = undefined,
    toJSON: dynamic = undefined,
    toObject: dynamic = undefined,
    typeKey: String? = undefined,
    typePojoToMixed: Boolean? = undefined,
    useNestedStrict: Boolean? = undefined,
    validateBeforeSave: Boolean? = undefined,
    versionKey: String? = undefined,
    collation: dynamic = undefined,
    selectPopulatedPaths: Boolean? = undefined,
    skipVersioning: dynamic = undefined,
    timestamps: Boolean? = undefined,
    storeSubdocValidationError: Boolean? = undefined
): SchemaOptions = dynamicOfDefined(
    "autoIndex" to autoIndex,
    "autoCreate" to autoCreate,
    "bufferCommands" to bufferCommands,
    "capped" to capped,
    "collection" to collection,
    "id" to id,
    "_id" to _id,
    "minimize" to minimize,
    "read" to read,
    "writeConcern" to writeConcern,
    "shardKey" to shardKey,
    "strict" to strict,
    "strictQuery" to strictQuery,
    "toJSON" to toJSON,
    "toObject" to toObject,
    "typeKey" to typeKey,
    "typePojoToMixed" to typePojoToMixed,
    "useNestedStrict" to useNestedStrict,
    "validateBeforeSave" to validateBeforeSave,
    "versionKey" to versionKey,
    "collation" to collation,
    "selectPopulatedPaths" to selectPopulatedPaths,
    "skipVersioning" to skipVersioning,
    "timestamps" to timestamps,
    "storeSubdocValidationError" to storeSubdocValidationError
).unsafeCast<SchemaOptions>()

interface ChildSchemaDefinition {
    val schema: Schema
    val model: Model
}

fun Schema.index(vararg spec: Pair<String, Boolean>) =
    index(dynamicOf(spec.map { it.first to if (it.second) 1 else -1 }))

fun Schema.index(spec: Iterable<Pair<String, Boolean>>) =
    index(dynamicOf(spec.map { it.first to if (it.second) 1 else -1 }))

fun Schema.index(spec: Map<String, Boolean>) =
    index(dynamicOf(spec.map { it.key to if (it.value) 1 else -1 }))
