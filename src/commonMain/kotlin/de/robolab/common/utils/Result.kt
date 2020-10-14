package de.robolab.common.utils

sealed class Result<out T, out E> {
    val isOk: Boolean
        get() = this is Ok

    fun okOrThrow(): T = when (this) {
        is Ok -> value
        is Err -> if (error is Throwable) throw IllegalStateException("this is not ok", error) else  throw IllegalStateException("this is not ok")
    }

    fun okOrNull(): T? = when (this) {
        is Ok -> value
        is Err -> null
    }

    inline fun <X> ifOk(block: (value: T) -> X): X? {
        if (this is Ok) {
            return block(value)
        }
        return null
    }

    val isError: Boolean
        get() = this is Err

    fun err(): E = when (this) {
        is Ok -> throw IllegalStateException("this is not err")
        is Err -> error
    }

    fun errOrNull(): E? = when (this) {
        is Ok -> null
        is Err -> error
    }

    inline fun <X> ifErr(block: (error: E) -> X): X? {
        if (this is Err) {
            return block(error)
        }
        return null
    }

    inline fun <X> handle(ifOk: (T) -> X, ifErr: (E) -> X): X {
        return when (this) {
            is Ok -> ifOk(value)
            is Err -> ifErr(error)
        }
    }


    operator fun component1() = okOrNull()
    operator fun component2() = errOrNull()
}

class Ok<T, E>(val value: T) : Result<T, E>()

class Err<T, E>(val error: E) : Result<T, E>()


fun <T, E> T.toOk() = Ok<T, E>(this)
fun <T, E> E.toErr() = Err<T, E>(this)

fun <T : Any> T?.toResult() = if (this != null) Ok<T, Unit>(this) else Err(Unit)
fun <T : Any, E> T?.toResultOr(error: E) = if (this != null) Ok<T, Unit>(this) else Err(error)
fun <T : Any, E> T?.toResultOr(errorBuilder: () -> E) = if (this != null) Ok<T, Unit>(this) else Err(errorBuilder())

inline fun <T> runCatching(block: () -> T): Result<T, Throwable> {
    return try {
        Ok(block())
    } catch (e: Throwable) {
        Err(e)
    }
}

inline fun <T, reified E : Throwable> runCatchingTyped(block: () -> T): Result<T, E> {
    return try {
        Ok(block())
    } catch (e: Throwable) {
        if (e is E) {
            Err(e)
        } else {
            throw e
        }
    }
}
