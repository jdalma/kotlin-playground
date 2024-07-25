package _50_DesginPatterns.concurrency

import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

fun main() {
    runBlocking {
        val batman = actor<String> {
            for (c in channel) {
                println("배트맨이 ${c}을 처리하고 있습니다.")
                delay(100)
            }
        }

        val robin = actor<String> {
            for (c in channel) {
                println("로빈이 ${c}을 처리하고 있습니다.")
                delay(250)
            }
        }

        val epicFight = launch {
            for (villain in listOf("Jocker", "Bane", "Penguin", "Riddler", "Killer Croc")) {
                val result = select<Pair<String, String>> {
                    batman.onSend(villain) {
                        "Batman" to villain
                    }
                    robin.onSend(villain) {
                        "Robin" to villain
                    }
                }
                delay(90)
                println(result)
            }
        }
    }

}
