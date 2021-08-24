@file:Suppress("UnsafeCastFromDynamic", "UNUSED_PARAMETER", "REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE", "unused")

package de.robolab.server.externaljs.ioredis

import de.robolab.common.externaljs.*
import de.robolab.common.externaljs.events.EventEmitter
import de.robolab.common.jsutils.promise
import kotlinx.coroutines.await
import kotlin.js.Promise

val ioredis = js("require(\"ioredis\")")
private val redisCreator = js(
    "function (){" +
            "var Redis = require(\"ioredis\");" +
            "return function (port, host, options){" +
            "return new Redis(port, host, options)" +
            "}" +
            "}"
)()

typealias RedisStrResponse = Promise<String>
typealias RedisIntResponse = Promise<Int>
typealias RedisBulkStrResponse = Promise<String?>
typealias RedisTypedArrayResponse<T> = Promise<JSArray<T?>>
typealias RedisArrayResponse = Promise<JSArray<Any?>>

@Suppress("FunctionName")
external interface IRedisCommandReceiver {
    fun append(key: String, value: String): RedisStrResponse
    fun append(key: String, value: Int): RedisStrResponse
    fun auth(password: String): RedisStrResponse
    fun bgrewriteaof(): RedisStrResponse
    fun bgsave(): RedisStrResponse

    //bgsave schedule

    fun bitcount(key: String): RedisIntResponse
    fun bitcount(key: String, start: Int, end: Int): RedisIntResponse

    //bitfield

    fun bitop(operation: String, destkey: String, key: String, keys: JSArray<String>): RedisIntResponse
    fun bitpos(key: String, bit: Int): RedisIntResponse
    fun bitpos(key: String, bit: Int, start: Int): RedisIntResponse
    fun bitpos(key: String, bit: Int, start: Int, end: Int): RedisIntResponse
    fun blpop(key: String, keys: JSArray<String>, timeout: Int): RedisArrayResponse
    fun brpop(key: String, keys: JSArray<String>, timeout: Int): RedisArrayResponse
    fun brpoplpush(source: String, destination: String, timeout: Int): RedisBulkStrResponse
    fun bzpopmin(key: String, keys: JSArray<String>, timeout: Int): RedisArrayResponse
    fun bzpopmax(key: String, keys: JSArray<String>, timeout: Int): RedisArrayResponse

    //client
    //cluster
    //command
    //config

    fun dbsize(): RedisIntResponse
    fun decr(key: String): RedisIntResponse
    fun decrby(key: String, decrement: Int): RedisIntResponse
    fun del(key: String, keys: JSArray<String>): RedisIntResponse
    fun discard()
    fun dump(key: String)
    fun echo(message: String)

    //eval
    //evalsha

    fun exec(): Promise<JSArray<Any?>?>
    fun exists(key: String, keys: JSArray<String>): RedisIntResponse
    fun expire(key: String, seconds: Int): RedisIntResponse
    fun expireat(key: String, timestamp: Int): RedisIntResponse
    fun flushall(): RedisStrResponse
    fun flushall(async: String): RedisStrResponse
    fun flushdb(): RedisStrResponse
    fun flushdb(async: String): RedisStrResponse
    fun geoadd(key: String, longitude: Float, latitude: Float, member: String): RedisIntResponse
    fun geohash(key: String, member: String, members: JSArray<String>): RedisTypedArrayResponse<String>
    fun geopos(
        key: String,
        member: String,
        members: List<String>
    ): RedisTypedArrayResponse<JSArray<Float>> // maybe string?

    fun geodist(key: String, member1: String, member2: String): RedisBulkStrResponse
    fun geodist(key: String, member1: String, member2: String, unit: String): RedisBulkStrResponse
    fun georadius(
        key: String,
        longitude: Float,
        latitude: Float,
        radius: Float,
        unit: String,
        vararg options: String
    ): RedisArrayResponse

    fun georadius_ro(
        key: String,
        longitude: Float,
        latitude: Float,
        radius: Float,
        unit: String,
        vararg options: String
    ): RedisArrayResponse

    fun georadiusbymember(
        key: String,
        member: String,
        radius: Float,
        unit: String,
        vararg options: String
    ): RedisArrayResponse

    fun georadiusbymember_ro(
        key: String,
        member: String,
        radius: Float,
        unit: String,
        vararg options: String
    ): RedisArrayResponse

    fun get(key: String): RedisBulkStrResponse
    fun getbit(key: String, offset: Int): RedisIntResponse
    fun getrange(key: String, start: Int, end: Int): RedisBulkStrResponse
    fun getset(key: String, value: String): RedisBulkStrResponse
    fun getset(key: String, value: Int): RedisBulkStrResponse
    fun getset(key: String, value: Float): RedisBulkStrResponse
    fun hdel(key: String, field: String, fields: JSArray<String>): RedisIntResponse

    //hello

    fun hexists(key: String, field: String): RedisIntResponse
    fun hget(key: String, field: String): RedisBulkStrResponse
    fun hgetall(key: String): RedisArrayResponse
    fun hincrby(key: String, field: String, increment: Int): RedisIntResponse
    fun hincrbyfloat(key: String, field: String, increment: Float): RedisBulkStrResponse
    fun hkeys(key: String): RedisTypedArrayResponse<String>
    fun hlen(key: String): RedisIntResponse
    fun hmget(key: String, field: String, fields: JSArray<String>): RedisTypedArrayResponse<String>
    fun hset(key: String, field: String, value: String): RedisIntResponse
    fun hset(key: String, field: String, value: Int): RedisIntResponse
    fun hset(key: String, field: String, value: Float): RedisIntResponse
    fun hset(key: String, field: String, value: String, args: JSArray<String>): RedisIntResponse
    fun hsetNX(key: String, field: String, value: String): RedisIntResponse
    fun hsetNX(key: String, field: String, value: Int): RedisIntResponse
    fun hsetNX(key: String, field: String, value: Float): RedisIntResponse
    fun hsetNX(key: String, field: String, value: String, args: JSArray<String>): RedisIntResponse
    fun hstrlen(key: String, field: String): RedisIntResponse
    fun hvals(key: String): RedisTypedArrayResponse<String>
    fun incr(key: String): RedisIntResponse
    fun incrby(key: String, increment: Int): RedisIntResponse
    fun incrbyfloat(key: String, increment: Float): RedisBulkStrResponse
    fun info(): RedisBulkStrResponse
    fun info(section: String): RedisBulkStrResponse
    fun keys(pattern: String): RedisTypedArrayResponse<String>
    fun lastsave(): RedisIntResponse
    fun lindex(key: String, index: Int): RedisBulkStrResponse
    fun linsert(key: String, location: String, pivot: String, element: String): RedisIntResponse
    fun llen(key: String): RedisIntResponse
    fun lpop(key: String): RedisBulkStrResponse

    //lpos

    fun lpush(key: String, element: String): RedisIntResponse
    fun lpush(key: String, element: Int): RedisIntResponse
    fun lpush(key: String, element: Float): RedisIntResponse
    fun lpush(key: String, element: String, elements: JSArray<String>): RedisIntResponse
    fun lpush(key: String, element: Int, elements: JSArray<Int>): RedisIntResponse
    fun lpush(key: String, element: Float, elements: JSArray<Float>): RedisIntResponse
    fun lpushx(key: String, element: String): RedisIntResponse
    fun lpushx(key: String, element: Int): RedisIntResponse
    fun lpushx(key: String, element: Float): RedisIntResponse
    fun lpushx(key: String, element: String, elements: JSArray<String>): RedisIntResponse
    fun lpushx(key: String, element: Int, elements: JSArray<Int>): RedisIntResponse
    fun lpushx(key: String, element: Float, elements: JSArray<Float>): RedisIntResponse
    fun lrange(key: String, start: Int, stop: Int): RedisArrayResponse
    fun lrem(key: String, count: Int, element: String): RedisIntResponse
    fun lrem(key: String, count: Int, element: Int): RedisIntResponse
    fun lrem(key: String, count: Int, element: Float): RedisIntResponse
    fun lset(key: String, index: Int, element: String): RedisStrResponse
    fun lset(key: String, index: Int, element: Int): RedisStrResponse
    fun lset(key: String, index: Int, element: Float): RedisStrResponse
    fun ltrim(key: String, start: Int, stop: Int): RedisStrResponse

    // memory

    fun mget(key: String, keys: JSArray<String>): RedisTypedArrayResponse<String?>

    //migrate
    //module

    fun move(key: String, db: Int): RedisIntResponse
    fun mset(key: String, value: String): RedisStrResponse
    fun mset(key: String, value: Int): RedisStrResponse
    fun mset(key: String, value: Float): RedisStrResponse
    fun mset(key: String, value: String, args: JSArray<String>): RedisStrResponse
    fun msetnx(key: String, value: String): RedisIntResponse
    fun msetnx(key: String, value: Int): RedisIntResponse
    fun msetnx(key: String, value: Float): RedisIntResponse
    fun msetnx(key: String, value: String, args: JSArray<String>): RedisIntResponse

    //fun multi(): RedisStrResponse // TODO: Multi apparently has special pipeline-behaviour in ioredis
    fun `object`(subcommand: String, arguments: JSArray<String>): Promise<Any?>
    fun persist(key: String): RedisIntResponse
    fun pexpire(key: String, milliseconds: Int): RedisIntResponse
    fun pexpireat(key: String, millisecondstimestampt: Int): RedisIntResponse
    fun pfadd(key: String, element: String, elements: JSArray<String>): RedisIntResponse
    fun pfcount(key: String, keys: JSArray<String>): RedisIntResponse
    fun pfmerge(destKey: String, sourceKey: String, sourceKeys: JSArray<String>): RedisStrResponse
    fun ping(): RedisStrResponse
    fun ping(message: String): RedisStrResponse
    fun psetex(key: String, milliseconds: Int, value: String): RedisStrResponse
    fun psubscribe(pattern: String, patterns: JSArray<String>)
    fun pubsub(subcommand: String, arguments: JSArray<String>): Promise<Any>
    fun pttl(key: String): RedisIntResponse
    fun publish(channel: String, message: String): RedisIntResponse
    fun punsubscribe(patterns: JSArray<String>)
    fun quit(): RedisStrResponse
    fun randomkey(): RedisBulkStrResponse
    fun readonly(): RedisStrResponse
    fun readwrite(): RedisStrResponse
    fun rename(key: String, newkey: String): RedisStrResponse
    fun renamenx(key: String, newkey: String): RedisIntResponse
    fun restore(key: String, ttl: Int, value: String): RedisStrResponse
    fun role(): RedisArrayResponse
    fun rpop(key: String): RedisBulkStrResponse
    fun rpoplpush(source: String, destination: String): RedisBulkStrResponse
    fun rpush(key: String, element: String): RedisIntResponse
    fun rpush(key: String, element: Int): RedisIntResponse
    fun rpush(key: String, element: Float): RedisIntResponse
    fun rpush(key: String, element: String, elements: JSArray<String>): RedisIntResponse
    fun rpush(key: String, element: Int, elements: JSArray<Int>): RedisIntResponse
    fun rpush(key: String, element: Float, elements: JSArray<Float>): RedisIntResponse
    fun rpushx(key: String, element: String): RedisIntResponse
    fun rpushx(key: String, element: Int): RedisIntResponse
    fun rpushx(key: String, element: Float): RedisIntResponse
    fun rpushx(key: String, element: String, elements: JSArray<String>): RedisIntResponse
    fun rpushx(key: String, element: Int, elements: JSArray<Int>): RedisIntResponse
    fun rpushx(key: String, element: Float, elements: JSArray<Float>): RedisIntResponse
    fun sadd(key: String, member: String, members: JSArray<String>): RedisIntResponse
    fun save(): RedisStrResponse
    fun scard(): RedisIntResponse
    fun scan(cursor: String): RedisArrayResponse
    fun scan(cursor: String, matchLiteral: String, pattern: String): RedisArrayResponse
    fun sscan(key: String, cursor: String): RedisArrayResponse
    fun sscan(key: String, cursor: String, matchLiteral: String, pattern: String): RedisArrayResponse
    fun hscan(key: String, cursor: String): RedisArrayResponse
    fun hscan(key: String, cursor: String, matchLiteral: String, pattern: String): RedisArrayResponse
    fun zscan(key: String, cursor: String): RedisArrayResponse
    fun zscan(key: String, cursor: String, matchLiteral: String, pattern: String): RedisArrayResponse

    //script

    fun sdiff(key: String, keys: JSArray<String>): RedisArrayResponse
    fun sdiffstore(destination: String, key: String, keys: JSArray<String>): RedisIntResponse
    fun select(index: Int): RedisStrResponse
    fun set(key: String, value: String): RedisStrResponse
    fun set(key: String, value: Int): RedisStrResponse
    fun set(key: String, value: Float): RedisStrResponse
    fun set(key: String, value: String, vararg options: String): RedisBulkStrResponse
    fun set(key: String, value: Int, vararg options: String): RedisBulkStrResponse
    fun set(key: String, value: Float, vararg options: String): RedisBulkStrResponse
    fun setbit(key: String, offset: Int, value: Int): RedisIntResponse
    fun setex(key: String, seconds: Int, value: String): RedisStrResponse
    fun setex(key: String, seconds: Int, value: Int): RedisStrResponse
    fun setex(key: String, seconds: Int, value: Float): RedisStrResponse
    fun setnx(key: String, value: String): RedisIntResponse
    fun setnx(key: String, value: Int): RedisIntResponse
    fun setnx(key: String, value: Float): RedisIntResponse
    fun setrange(key: String, offset: Int, value: String): RedisIntResponse
    fun shutdown(): RedisStrResponse?
    fun shutdown(modifier: String): RedisStrResponse?
    fun sinter(key: String, keys: JSArray<String>): RedisArrayResponse
    fun sinterstore(destination: String, key: String, keys: JSArray<String>): RedisIntResponse
    fun sismember(key: String, member: String): RedisIntResponse

    //slaveof
    //slowlog

    fun smembers(key: String): RedisTypedArrayResponse<String>
    fun smove(source: String, destination: String, member: String): RedisIntResponse
    fun sort(key: String, args: JSArray<String>): Promise<Any>
    fun spop(key: String): RedisBulkStrResponse
    fun spop(key: String, count: Int): RedisTypedArrayResponse<String>
    fun srandmember(key: String): RedisBulkStrResponse
    fun srandmember(key: String, count: Int): RedisTypedArrayResponse<String>
    fun srem(key: String, member: String, members: JSArray<String>): RedisIntResponse
    fun strlen(key: String): RedisIntResponse
    fun subscribe(channel: String, channels: JSArray<String>)
    fun sunion(key: String, keys: JSArray<String>): RedisTypedArrayResponse<String>
    fun sunionstore(destination: String, key: String, keys: JSArray<String>): RedisIntResponse
    fun swapdb(index1: Int, index2: Int): RedisStrResponse

    //sync
    //psync

    fun time(): RedisTypedArrayResponse<String>
    fun touch(key: String, keys: JSArray<String>): RedisIntResponse
    fun ttl(key: String): RedisIntResponse

    //type

    fun unlink(key: String, keys: JSArray<String>): RedisIntResponse
    fun unsubscribe(channel: JSArray<String>)
    fun unwatch(): RedisStrResponse
    fun wait(): RedisIntResponse
    fun watch(key: String, keys: JSArray<String>): RedisStrResponse

    /*
    fun xack(key:String, group:String, id:String,  ids:JSArray<String>):RedisIntResponse
    fun xadd(key:String, id:String, field:String, value:String,  args:JSArray<String>):RedisBulkStrResponse
    fun xclaim()
    fun xdel()
    fun xgroup()

    //xinfo

    fun xlen()
    fun xpending()
    fun xrange()
    fun xread()
    fun xreadgroup()
    fun xrevrange()
    fun xsetid()
    fun xtrim()
    */

    fun zadd(key: String, score: Float, member: String): RedisIntResponse
    fun zadd(key: String, score: String, member: String): RedisIntResponse
    fun zadd(key: String, vararg options: String): Promise<Any>
    fun zcard(key: String): RedisIntResponse
    fun zcount(key: String, min: String, max: String): RedisIntResponse
    fun zincrby(key: String, increment: Float, member: String): RedisBulkStrResponse
    fun zinterstore(destination: String, numkeys: Int, key: String, keys: JSArray<String>): RedisIntResponse
    fun zinterstore(destination: String, vararg options: String): RedisIntResponse
    fun zlexcount(key: String, min: String, max: String): RedisIntResponse
    fun zpopmax(key: String): RedisArrayResponse
    fun zpopmax(key: String, count: Int): RedisArrayResponse
    fun zpopmin(key: String): RedisArrayResponse
    fun zpopmin(key: String, count: Int): RedisArrayResponse
    fun zrange(key: String, start: Int, stop: Int): RedisArrayResponse
    fun zrange(key: String, start: Int, stop: Int, withscoresLiteral: String): RedisArrayResponse
    fun zrangebylex(key: String, min: Float, max: Float): RedisArrayResponse
    fun zrangebylex(
        key: String,
        min: Float,
        max: Float,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrangebyscore(key: String, min: String, max: String): RedisArrayResponse
    fun zrangebyscore(key: String, min: String, max: String, withscoresLiteral: String): RedisArrayResponse
    fun zrangebyscore(
        key: String,
        min: String,
        max: String,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrangebyscore(
        key: String,
        min: String,
        max: String,
        withscoresLiteral: String,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrank(key: String, member: String): Promise<Int?>
    fun zrem(key: String, member: String, members: JSArray<String>): RedisIntResponse
    fun zremrangebylex(key: String, min: String, max: String): RedisIntResponse
    fun zremrangebyrank(key: String, start: Int, stop: Int): RedisIntResponse
    fun zremrangebyscore(key: String, min: String, max: String): RedisIntResponse
    fun zrevrange(key: String, start: Int, stop: Int): RedisArrayResponse
    fun zrevrange(key: String, start: Int, stop: Int, withscoresLiteral: String): RedisArrayResponse
    fun zrevrangebylex(key: String, min: Float, max: Float): RedisArrayResponse
    fun zrevrangebylex(
        key: String,
        min: Float,
        max: Float,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrevrangebyscore(key: String, min: String, max: String): RedisArrayResponse
    fun zrevrangebyscore(key: String, min: String, max: String, withscoresLiteral: String): RedisArrayResponse
    fun zrevrangebyscore(
        key: String,
        min: String,
        max: String,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrevrangebyscore(
        key: String,
        min: String,
        max: String,
        withscoresLiteral: String,
        limitLiteral: String,
        offset: Int,
        count: Int
    ): RedisArrayResponse

    fun zrevrank(key: String, member: String): Promise<Int?>
    fun zscore(key: String, member: String): RedisBulkStrResponse
    fun zunionstore(destination: String, numkeys: Int, key: String, keys: JSArray<String>): RedisIntResponse
    fun zunionstore(destination: String, vararg options: String): RedisIntResponse
}

fun IRedisCommandReceiver.mget(keys: JSArray<String>): RedisTypedArrayResponse<String> {
    return when (keys.length) {
        0 -> Promise.Companion.resolve(emptyJSArray())
        1 -> mget(keys[0], emptyJSArray())
        else -> mget(keys[0], keys.toList().drop(1).toJSArray())
    }
}

fun IRedisCommandReceiver.mget(keys: List<String>): RedisTypedArrayResponse<String> {
    return when {
        keys.isEmpty() -> Promise.Companion.resolve(emptyJSArray())
        keys.size == 1 -> mget(keys.single(), emptyJSArray())
        else -> {
            //Unexpected compile error (probably related to spread over constructed array)
            //mget(keys[0], *keys.slice(IntRange(1, keys.lastIndex)).toTypedArray())
            promise {
                val proms = keys.map { get(it) }
                return@promise proms.map { it.await() }.toJSArray()
            }
        }
    }
}

fun IRedisCommandReceiver.mset(args: List<Pair<String, String>>): RedisStrResponse {
    return when {
        args.isEmpty() -> Promise.Companion.resolve("OK")
        args.size == 1 -> mset(args.single().first, args.single().second)
        else -> {
            mset(
                args[0].first,
                args[0].second,
                args.slice(IntRange(1, args.lastIndex)).flatMap { listOf(it.first, it.second) }.toJSArray()
            )
        }
    }
}

fun IRedisCommandReceiver.mset(args: Map<String, String>): RedisStrResponse {
    return when {
        args.isEmpty() -> Promise.Companion.resolve("OK")
        args.size == 1 -> mset(args.entries.single().key, args.entries.single().value)
        else -> mset(args.toList())
    }
}


fun IRedisCommandReceiver.msetnx(args: List<Pair<String, String>>): RedisIntResponse {
    return when {
        args.isEmpty() -> Promise.Companion.resolve(0)
        args.size == 1 -> msetnx(args.single().first, args.single().second)
        else -> {
            msetnx(
                args[0].first,
                args[0].second,
                args.slice(IntRange(1, args.lastIndex)).flatMap { listOf(it.first, it.second) }.toJSArray()
            )
        }
    }
}

fun IRedisCommandReceiver.msetnx(args: Map<String, String>): RedisIntResponse {
    return when {
        args.isEmpty() -> Promise.Companion.resolve(0)
        args.size == 1 -> msetnx(args.entries.single().key, args.entries.single().value)
        else -> msetnx(args.toList())
    }
}


fun IRedisCommandReceiver.setxx(key: String, value: String): RedisBulkStrResponse = set(key, value, "XX")
fun IRedisCommandReceiver.setxx(key: String, value: Int): RedisBulkStrResponse = set(key, value, "XX")
fun IRedisCommandReceiver.setxx(key: String, value: Float): RedisBulkStrResponse = set(key, value, "XX")

fun createRedis(): Redis = redisCreator()
fun createRedis(port: Int): Redis = redisCreator(port)
fun createRedis(url: String): Redis = redisCreator(url)
fun createRedis(port: Int, host: String): Redis = redisCreator(port, host)
fun createRedis(url: String, options: dynamic): Redis = redisCreator(url, options)
fun createRedis(port: Int, host: String, options: dynamic): Redis = redisCreator(port, host, options)

abstract external class Redis : IRedisCommandReceiver {
    fun connect(): Promise<Unit>
    fun disconnect()
    fun duplicate(): Redis
    fun monitor(): Promise<EventEmitter>
    fun getBuiltinCommands(): JSArray<String>

    fun on(event: String, handler: (event: dynamic) -> dynamic)
}

fun IRedisCommandReceiver.del(key: String) = del(key, emptyJSArray())
fun IRedisCommandReceiver.del(keys: List<String>) = del(keys.first(), keys.drop(1).toJSArray())

fun IRedisCommandReceiver.lpush(key: String, elements: List<String>) =
    lpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.lpush(key: String, elements: List<Int>) =
    lpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.lpush(key: String, elements: List<Float>) =
    lpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.lpushx(key: String, elements: List<String>) =
    lpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.lpushx(key: String, elements: List<Int>) =
    lpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.lpushx(key: String, elements: List<Float>) =
    lpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpush(key: String, elements: List<String>) =
    rpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpush(key: String, elements: List<Int>) =
    rpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpush(key: String, elements: List<Float>) =
    rpush(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpushx(key: String, elements: List<String>) =
    rpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpushx(key: String, elements: List<Int>) =
    rpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.rpushx(key: String, elements: List<Float>) =
    rpushx(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.sadd(key: String, elements: List<String>) =
    sadd(key, elements.first(), elements.drop(1).toJSArray())

fun IRedisCommandReceiver.srem(key: String, elements: List<String>) =
    srem(key, elements.first(), elements.drop(1).toJSArray())

suspend inline fun IRedisCommandReceiver.transaction(
    watches: JSArray<String>,
    watchedBlock: suspend IRedisCommandReceiver.() -> Unit,
    block: IRedisCommandReceiver.() -> Unit
): JSArray<Any?> {
    /*var result: JSArray<Any?>?
    do {
        //Unexpected compile error (probably related to spread over constructed array)
        //if (watches.isNotEmpty())
        //    watch(watches[0], *watches.sliceArray(IntRange(1, watches.lastIndex)))
        for(watch in watches)
            this.watch(watch)
        var success = false
        try {
            watchedBlock()
            success = true
        } finally {// not catch + rethrow because of stacktrace-hell
            if (!success)
                unwatch()
        }
        success = false
        multi()
        try {
            block()
            success = true
        } finally {// not catch + rethrow because of stacktrace-hell
            if (!success)
                discard()
        }
        result = exec().await()
    } while (result == null)
    return result*/
    throw NotImplementedError("Multi has custom pipelining behaviour in ioredis, will require further investigation")
}

suspend inline fun IRedisCommandReceiver.transaction(block: IRedisCommandReceiver.() -> Unit): JSArray<Any?> {
    /*var result: JSArray<Any?>?
    do {
        println("Starting multi")
        multi()
        println("Started multi")
        var success = false
        try {
            println("Attempting block")
            block()
            println("Block success")
            success = true
        } finally { // not catch + rethrow because of stacktrace-hell
            if (!success){
                println("Discarding")
                discard()
            }
        }
        println("Starting Exec")
        result = exec().await()
        println("Finished Exec: $result")
    } while (result == null)
    println("Finished transaction")
    return result*/
    throw NotImplementedError("Multi has custom pipelining behaviour in ioredis, will require further investigation") //TODO: Write DSL for pipelining
}