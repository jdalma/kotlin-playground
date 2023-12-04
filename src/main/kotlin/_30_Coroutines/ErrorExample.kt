package _30_Coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
//    val details = try {
//        getUserDetails()
//    } catch (e: Error) {
//        null
//    }
    val details = getUserDetails()

    val tweets = async { getTweets() }
    println("User: $details")
    println("Tweets: ${tweets.await()}")
}

suspend fun getUserName(): String = "userName"
suspend fun getTweets(): List<String> = listOf("a", "b")
fun getFollowerNumber(): Int = throw Error("Service Exception")

suspend fun CoroutineScope.getUserDetails(): Pair<String, Int> {
    val userName = async { getUserName() }
    val followersNumber = async { getFollowerNumber() }
    return userName.await() to followersNumber.await()
}
