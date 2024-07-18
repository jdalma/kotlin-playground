package _50_DesginPatterns.behavioral

fun main() {
    val req = Request(
        "developer@company.com",
        "Why do we need Software Architects?"
    )

    val chain = basicValidation(authentication(finalResponse()))

    val res = chain(req)

    println(res)
}

typealias Handler = (request: Request) -> Response

val authentication: (Handler) -> (Request) -> Response = fun(next: Handler): (Request) -> Response =
    fun(request: Request): Response {
        if (!request.isKnownEmail()) {
            throw IllegalArgumentException()
        }
        return next(request)
    }

val basicValidation: (Handler) -> (Handler) = fun(next: Handler): Handler =
    fun(request: Request): Response {
        if (request.email.isEmpty() || request.question.isEmpty()) {
            throw IllegalArgumentException()
        }
        return next(request)
    }

val finalResponse = fun() =
    fun(request: Request): Response {
        return Response("I don't know")
    }

data class Request(val email: String, val question: String) {
    fun isKnownEmail(): Boolean {
        return true
    }

    fun isFromJuniorDeveloper(): Boolean {
        return false
    }
}

data class Response(val answer: String)
