@file:JsModule("mongoose")
@file:Suppress("PropertyName")

package de.robolab.server.externaljs.mongoose

import de.robolab.server.externaljs.DynJSArray
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.JSObject
import de.robolab.server.externaljs.events.EventEmitter
import de.robolab.server.jsutils.JSDynErrorCallback
import kotlin.js.Promise
import kotlin.js.RegExp

external class Mongoose {
    val Date: SchemaType
    val Decimal128: SchemaType
    val Mixed: SchemaType
    val Number: SchemaType
    val ObjectId: SchemaType

    val connection: Connection
    val connections: JSArray<Connection>
    val driver: dynamic
    val mongo: dynamic
    val mquery: dynamic
    val version: String

    val STATES: dynamic
    val SchemaTypes: dynamic
    val Types: dynamic


    fun connect(uri: String, options: dynamic = definedExternally): Promise<Mongoose>
    fun createConnection(uri: String, options: dynamic = definedExternally): Promise<Mongoose>

    fun deleteModel(name: String): Mongoose
    fun deleteModel(name: RegExp): Mongoose

    fun disconnect(callback: JSDynErrorCallback = definedExternally)

    fun get(key: String): Any?
    fun isValidObjectId(id: dynamic): Boolean
    fun isValidObjectId(id: Any?): Boolean

    fun model(
        name: String,
        schema: Schema? = definedExternally,
        collection: String? = definedExternally,
        skipInit: Boolean? = definedExternally
    ): Model

    fun modelNames(): JSArray<String>

    fun set(key: String, value: Any?)
}

external class Schema(definition: dynamic, options: SchemaOptions = definedExternally) {
    constructor(definition: Schema, options: SchemaOptions = definedExternally)
    constructor(array: JSArray<dynamic>, options: SchemaOptions = definedExternally)

    companion object {
        object Types {
            val String: SchemaType
            val Number: SchemaType
            val Boolean: SchemaType
            val Array: SchemaType
            val Buffer: SchemaType
            val Date: SchemaType
            val ObjectId: SchemaType
            val Mixed: SchemaType
        }

        val indexTypes: dynamic
    }

    val childSchemas: JSArray<ChildSchemaDefinition>
    val obj: dynamic
    val paths: JSObject<String, dynamic> //Unsure: Schema or SchemaType
    val reserved: JSObject<String, *>

    fun add(obj: dynamic, prefix: String? = definedExternally): Schema
    fun add(obj: Schema, prefix: String? = definedExternally): Schema

    fun clone(): Schema

    fun eachPath(fn: (pathname: String, schematype: SchemaType) -> Unit): Schema

    fun get(key: String): Any?

    fun index(fields: dynamic, options: dynamic = definedExternally)

    fun indexes(): JSArray<dynamic>

    fun method(method: String, fn: () -> dynamic)
    fun method(method: JSObject<String, () -> dynamic>)

    fun path(path: String): SchemaType?
    fun path(path: String, constructor: SchemaType)

    fun pathType(path: String): dynamic

    fun pick(paths: JSArray<String>, options: SchemaOptions = definedExternally): Schema

    fun post(name: String, fn: (dynamic) -> Unit)
    fun post(name: String, options: dynamic, fn: (dynamic) -> Unit)
    fun post(name: RegExp, fn: (dynamic) -> Unit)
    fun post(name: RegExp, options: dynamic, fn: (dynamic) -> Unit)
    fun pre(name: String, callback: (dynamic) -> Unit)
    fun pre(name: String, options: dynamic, callback: (dynamic) -> Unit)
    fun pre(name: RegExp, callback: (dynamic) -> Unit)
    fun pre(name: RegExp, options: dynamic, callback: (dynamic) -> Unit)

    fun queue(name: String, args: JSArray<*>)

    fun remove(path: String): Schema
    fun remove(path: JSArray<String>): Schema

    fun requiredPaths(invalidate: Boolean = definedExternally): JSArray<String>

    fun set(key: String): Any?
    fun set(key: String, value: Any?)

    fun static(method: String, fn: () -> dynamic)
    fun static(method: JSObject<String, () -> dynamic>)

    fun virtual(name: String): VirtualType

    fun virtualpath(name: String): VirtualType
}

external interface Connection {
    val collections: dynamic
    val config: dynamic
    val db: dynamic
    val host: String
    val id: Int
    val models: JSObject<String, Model>
    val name: String
    val pass: String?
    val port: Int
    val readyState: Int
    val user: String?

    fun close(force: Boolean = definedExternally): Promise<*>

    fun collection(name: String, options: dynamic = definedExternally): Collection

    fun createCollection(collection: String, options: dynamic = definedExternally): Promise<Collection>

    fun deleteModel(name: String): Connection
    fun deleteModel(name: RegExp): Connection

    fun dropCollection(collection: String): Promise<*>

    fun dropDatabase(): Promise<*>

    fun get(key: String): Any?

    fun model(name: String, schema: Schema? = definedExternally, collection: String? = definedExternally): Model

    fun modelNames(): JSArray<String>

    fun openUri(uri: String, options: dynamic = definedExternally): Promise<*>

    fun set(key: String, `val`: Any?)

    fun useDb(name: String, options: dynamic = definedExternally): Connection

    fun watch(): EventEmitter
}

external interface Document {
    val `$locals`: dynamic
    val `$op`: String?
    val errors: dynamic
    val id: String?
    val isNew: Boolean
    val schema: Schema

    fun `$ignore`(path: String)

    fun `$isDefault`(path: String? = definedExternally): Boolean

    fun `$isDeleted`(): Boolean
    fun `$isDeleted`(`val`: Boolean?)

    fun `$isEmpty`(): Boolean

    fun `$markValid`(path: String)

    fun `$session`(): dynamic
    fun `$session`(session: dynamic): dynamic

    fun `$set`(path: String, `val`: Any?)
    fun `$set`(path: JSObject<String, Any?>)
    fun `$set`(path: dynamic)

    fun depopulate(path: String): Document

    fun directModifiedPaths(): JSArray<String>

    fun equals(doc: Document): Boolean

    fun execPopulate(): Promise<Document>

    fun get(path: String): Any?

    fun inspect()

    fun invalidate(path: String, errorMsg: String, value: Any? = definedExternally)
    fun invalidate(path: String, errorMsg: Error, value: Any? = definedExternally)
    fun invalidate(path: String, errorMsg: dynamic, value: Any? = definedExternally)

    fun isDirectModified(path: String): Boolean

    fun isDirectSelected(path: String): Boolean

    fun isInit(path: String): Boolean

    fun isModified(): Boolean
    fun isModified(path: String): Boolean

    fun isSelected(path: String): Boolean

    fun markModified(path: String, scope: Document? = definedExternally)

    fun modifiedPaths(options: dynamic = definedExternally): JSArray<String>

    fun overwrite(obj: dynamic)

    fun populate(): Document
    fun populate(path: String): Document
    fun populate(path: dynamic): Document

    fun populated(path: String): dynamic

    fun replaceOne(doc: dynamic, options: dynamic = definedExternally): Query //Maybe Promise<Query> ?

    fun save(options: dynamic = definedExternally): Promise<*>

    fun set(path: String, `val`: Any?)
    fun set(path: JSObject<String, Any?>)
    fun set(path: dynamic)

    fun toJSON(options: dynamic = definedExternally): dynamic
    fun toObject(options: dynamic = definedExternally): dynamic

    fun unmarkModified(path: String)

    fun update(doc: dynamic, options: dynamic = definedExternally): Query //Maybe Promise<Query>?

    fun updateOne(doc: dynamic, options: dynamic = definedExternally): Query //Maybe Promise<Query>?

    fun validate(): Promise<*>
    fun validate(pathsToValidate: String, options: dynamic = definedExternally): Promise<*>
    fun validate(pathsToValidate: JSArray<String>, options: dynamic = definedExternally): Promise<*>

    fun validateSync(): Any?
    fun validateSync(pathsToValidate: String, options: dynamic = definedExternally): Any?
    fun validateSync(pathsToValidate: JSArray<String>, options: dynamic = definedExternally): Any?
}

external interface Model {
    val events: EventEmitter
    val base: dynamic
    val baseModelName: String?
    val collection: Collection
    val db: Connection
    val discriminators: dynamic
    val modelName: String
    val schema: dynamic

    operator fun invoke(): Document

    fun aggregate(pipeline: DynJSArray? = definedExternally): Aggregate

    fun bulkWrite(ops: JSArray<dynamic>, options: dynamic = definedExternally): Promise<dynamic>

    fun cleanIndexes(): Promise<*>

    fun countDocuments(filter: dynamic): Query

    fun create(docs: JSArray<dynamic>, options: dynamic = definedExternally): Promise<*>
    fun create(vararg docs: dynamic, options: dynamic = definedExternally): Promise<*>

    fun createCollection(options: dynamic = definedExternally): Promise<Collection>

    fun createIndexes(options: dynamic = definedExternally): Promise<*>

    fun deleteMany(conditions: dynamic, options: dynamic = definedExternally): Query

    fun deleteOne(conditions: dynamic, options: dynamic = definedExternally): Query

    fun discriminator(name: String, schema: Schema, value: String? = definedExternally): Model

    fun distinct(field: String, conditions: dynamic = definedExternally): Query

    fun ensureIndexes(options: dynamic = definedExternally): Promise<*>

    fun estimatedDocumentCount(options: dynamic = definedExternally): Query

    fun exists(filter: dynamic): Promise<Boolean>

    fun find(filter: dynamic, projection: dynamic = definedExternally, options: dynamic = definedExternally): Query
    fun find(filter: dynamic, projection: String = definedExternally, options: dynamic = definedExternally): Query

    fun findById(id: Any, projection: dynamic = definedExternally, options: dynamic = definedExternally): Query
    fun findById(id: Any, projection: String = definedExternally, options: dynamic = definedExternally): Query

    fun findByIdAndDelete(id: Int, options: dynamic = definedExternally): Query
    fun findByIdAndDelete(id: String, options: dynamic = definedExternally): Query
    fun findByIdAndDelete(id: Any, options: dynamic = definedExternally): Query

    fun findByIdAndRemove(id: Int, options: dynamic = definedExternally): Query
    fun findByIdAndRemove(id: String, options: dynamic = definedExternally): Query
    fun findByIdAndRemove(id: Any, options: dynamic = definedExternally): Query


    fun findByIdAndUpdate(id: Int, update: dynamic, options: dynamic = definedExternally): Query
    fun findByIdAndUpdate(id: String, update: dynamic, options: dynamic = definedExternally): Query
    fun findByIdAndUpdate(id: Any, update: dynamic, options: dynamic = definedExternally): Query

    fun findOne(
        conditions: dynamic = definedExternally,
        projection: dynamic = definedExternally,
        options: dynamic = definedExternally
    ): Query

    fun findOne(
        conditions: dynamic = definedExternally,
        projection: String = definedExternally,
        options: dynamic = definedExternally
    ): Query

    fun findOneAndDelete(conditions: dynamic, options: dynamic = definedExternally): Query

    fun findOneAndRemove(conditions: dynamic, options: dynamic = definedExternally): Query

    fun findOneAndReplace(
        filter: dynamic,
        replacement: dynamic = definedExternally,
        options: dynamic = definedExternally
    ): Query

    fun findOneAndUpdate(
        conditions: dynamic = definedExternally,
        update: dynamic = definedExternally,
        options: dynamic = definedExternally
    ): Query

    fun geoSearch(conditions: dynamic, options: dynamic): Promise<*>

    fun hydrate(obj: dynamic): Document

    fun init(): Promise<*>

    fun insertMany(docs: dynamic, options: dynamic = definedExternally): Promise<*>
    fun insertMany(docs: JSArray<dynamic>, options: dynamic = definedExternally): Promise<*>
    fun insertMany(vararg docs: dynamic, options: dynamic = definedExternally): Promise<*>

    fun inspect(): String

    fun listIndexes(): Promise<dynamic>

    fun mapReduce(o: dynamic): Promise<*>

    fun populate(docs: Document, options: String = definedExternally): Promise<Document>
    fun populate(docs: Document, options: dynamic): Promise<Document>
    fun populate(docs: JSArray<Document>, options: String = definedExternally): Promise<Document>
    fun populate(docs: JSArray<Document>, options: dynamic): Promise<Document>

    fun `$where`(argument: String): Query

    fun delete(): Promise<*>  //Listed as property, sounds like a method though
    fun deleteOne(): Promise<*>  //Listed as property, sounds like a method though

    fun increment()

    fun model(name: String): Model

    fun remove(options: dynamic = definedExternally): Promise<*>

    fun save(options: dynamic = definedExternally): Promise<*>

    fun remove(conditions: dynamic, options: dynamic = definedExternally): Query

    fun replaceOne(filter: dynamic, doc: dynamic, options: dynamic = definedExternally): Query

    fun syncIndexes(options: dynamic = definedExternally): Promise<*>

    fun translateAliases(raw: dynamic): dynamic

    fun update(filter: dynamic, doc: dynamic, options: dynamic = definedExternally): Query

    fun updateMany(filter: dynamic, doc: dynamic, options: dynamic = definedExternally): Query

    fun updateOne(filter: dynamic, doc: dynamic, options: dynamic = definedExternally): Query

    fun validate(obj: dynamic, pathsToValidate: JSArray<String>, context: dynamic = definedExternally): Promise<*>

    fun watch(pipeline: DynJSArray? = definedExternally, options: dynamic = definedExternally): EventEmitter

    fun where(path: String, `val`: dynamic = definedExternally): Query
}

external interface Query

external interface Aggregate

external interface SchemaType

external interface VirtualType

external interface Collection
