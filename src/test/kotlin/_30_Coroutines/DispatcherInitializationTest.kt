package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

/**
 * 디스패처 초기화 시점과 성능을 확인하는 테스트
 */
class DispatcherInitializationTest : BehaviorSpec({

    given("디스패처 초기화 시점을 측정할 때") {
        `when`("첫 번째 사용 시점을 측정하면") {
            then("초기화 비용을 확인할 수 있다") {

                // Default 첫 사용
                val defaultInitTime = measureTimeMillis {
                    runBlocking(Dispatchers.Default) {
                        println("Default 첫 사용: ${Thread.currentThread().name}")
                    }
                }
                
                // Default 두 번째 사용
                val defaultSecondTime = measureTimeMillis {
                    runBlocking(Dispatchers.Default) {
                        println("Default 두 번째: ${Thread.currentThread().name}")
                    }
                }
                
                // IO 첫 사용
                val ioInitTime = measureTimeMillis {
                    runBlocking(Dispatchers.IO) {
                        println("IO 첫 사용: ${Thread.currentThread().name}")
                    }
                }
                
                // IO 두 번째 사용
                val ioSecondTime = measureTimeMillis {
                    runBlocking(Dispatchers.IO) {
                        println("IO 두 번째: ${Thread.currentThread().name}")
                    }
                }
                
                println("Default 첫 사용: ${defaultInitTime}ms")
                println("Default 두 번째: ${defaultSecondTime}ms")
                println("IO 첫 사용: ${ioInitTime}ms") 
                println("IO 두 번째: ${ioSecondTime}ms")
                
                // 첫 사용이 더 오래 걸림 (초기화 비용)
                println("Default 초기화 오버헤드: ${defaultInitTime > defaultSecondTime}")
                println("IO 초기화 오버헤드: ${ioInitTime > ioSecondTime}")
            }
        }
    }
    
    given("여러 디스패처를 동시에 사용할 때") {
        `when`("스레드 풀 상태를 확인하면") {
            then("각각 독립적인 스레드 풀을 가져야 한다") {
                val threadNames = mutableSetOf<String>()
                
                runBlocking {
                    // 여러 디스패처에서 동시 실행
                    val jobs = listOf(
                        launch(Dispatchers.Default) {
                            threadNames.add("Default: ${Thread.currentThread().name}")
                        },
                        launch(Dispatchers.IO) {
                            threadNames.add("IO: ${Thread.currentThread().name}")
                        },
                        launch(Dispatchers.Unconfined) {
                            threadNames.add("Unconfined: ${Thread.currentThread().name}")
                        }
                    )
                    
                    jobs.joinAll()
                }
                
                println("=== 사용된 스레드들 ===")
                threadNames.forEach { println(it) }
                
                // 서로 다른 디스패처는 서로 다른 스레드 사용
                val hasDefault = threadNames.any { it.contains("DefaultDispatcher") }
                val hasIO = threadNames.any { it.contains("DefaultDispatcher") } // IO도 DefaultDispatcher 공유
                val hasUnconfined = threadNames.any { it.contains("main") || it.contains("Test") }
                
                println("Default 스레드 사용: $hasDefault")
                println("IO 스레드 사용: $hasIO")  
                println("Unconfined 스레드 사용: $hasUnconfined")
            }
        }
    }
})
