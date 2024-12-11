@file:Suppress("LibraryEntitiesShouldNotBePublic")

package dev.reifiedbeans.result

import kotlin.reflect.KClass

sealed class Result<out T>(protected open val value: T?, protected open val error: Throwable?) {
    class Success<out T>(public override val value: T) : Result<T>(value, null)
    class Failure(public override val error: Throwable) : Result<Nothing>(null, error)

    /**
     * Returns the value of a [Success] or null if the result is a [Failure].
     * Exercise caution when using this method as it is impossible to distinguish a null value from a failure.
     * Prefer using [getOrElse] to handle errors explicitly.
     */
    fun getOrNull(): T? = this.value
}

@Suppress("TooGenericExceptionCaught")
inline fun <T> runCatching(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Throwable) {
    Result.Failure(e)
}

inline fun <R, T : R> Result<T>.getOrElse(onFailure: (Throwable) -> R): R = when (this) {
    is Result.Success -> this.value
    is Result.Failure -> onFailure(this.error)
}

inline fun <T, reified E : Throwable> Result<T>.onFailure(vararg errors: KClass<out E>, action: (E) -> Unit): Result<T> {
    // Empty assertion to "use" parameter, but the param is only there for type-checking
    require(errors.isNotEmpty())

    if (this is Result.Failure && this.error is E) action(this.error)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) action(this.error)
    return this
}
