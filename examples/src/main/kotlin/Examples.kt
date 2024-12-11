@file:Suppress("ReturnCount", "TooGenericExceptionThrown")

import dev.reifiedbeans.result.Result
import dev.reifiedbeans.result.getOrElse
import dev.reifiedbeans.result.onFailure
import dev.reifiedbeans.result.runCatching

fun funcThatThrows(s: String?): String {
    if (s == null) throw UnsupportedOperationException("Cannot perform operation on null String")
    require(s.isNotEmpty()) { "String must not be null" }

    throw RuntimeException("Something went wrong")
}

fun funcThatFails(s: String?): Result<String> {
    if (s == null) {
        return Result.Failure(UnsupportedOperationException("Cannot perform operation on null String"))
    } else if (s.isEmpty()) {
        return Result.Failure(IllegalArgumentException("String must not be null"))
    }

    return Result.Failure(RuntimeException("Something went wrong"))
}

fun doOperations(): Result<Unit> {
    val result1 = runCatching {
        funcThatThrows("")
    }.onFailure(UnsupportedOperationException::class, IllegalArgumentException::class) {
        return Result.Failure(IllegalStateException("Not able to complete this operation"))
    }.getOrNull()

    println("Ran first operation: $result1")

    val result2 = funcThatFails(null).getOrElse {
        return Result.Failure(RuntimeException("Something went wrong", it))
    }

    println("Ran second operation: $result2")

    return Result.Success(Unit)
}

fun main() {
    doOperations().onFailure {
        println("Something went wrong: $it")
    }
}
