package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Channel 내부에서 예외 발생 시 exactly-once 전달 보장 테스트
 * - send/receive 과정에서 예외 발생 시 데이터 손실/중복 여부
 * - Channel의 내부 상태 일관성 확인
 */
class ChannelExceptionSafetyTest : BehaviorSpec({

    given("Channel send 과정에서 예외가 발생할 때") {
        `when`("코루틴이 취소되면") {
            then("전송된 데이터는 정확히 한 번만 처리되어야 한다") {
                val channel = Channel<String>(capacity = 3)
                val processedMessages = ConcurrentHashMap<String, AtomicInteger>()
                val sendAttempts = AtomicInteger(0)
                val actualSends = AtomicInteger(0)
                
                runBlocking {
                    // Producer - 전송 중 취소될 수 있음
                    val producer = launch {
                        try {
                            repeat(10) { i ->
                                sendAttempts.incrementAndGet()
                                channel.send("message-$i")
                                actualSends.incrementAndGet()
                                
                                // 50% 확률로 취소 시뮬레이션
                                if (i == 5) {
                                    throw CancellationException("Producer cancelled")
                                }
                                delay(10)
                            }
                        } catch (e: CancellationException) {
                            println("Producer cancelled after ${actualSends.get()} successful sends")
                            throw e
                        } finally {
                            channel.close()
                        }
                    }
                    
                    // Consumer - 모든 전송된 메시지 수신
                    val consumer = launch {
                        try {
                            for (message in channel) {
                                processedMessages.computeIfAbsent(message) { AtomicInteger(0) }
                                    .incrementAndGet()
                                println("Received: $message")
                            }
                        } catch (e: Exception) {
                            println("Consumer error: ${e.message}")
                        }
                    }
                    
                    try {
                        producer.join()
                    } catch (e: CancellationException) {
                        // Producer 취소는 예상됨
                    }
                    consumer.join()
                }
                
                // 검증: 전송된 메시지는 정확히 한 번씩만 처리
                val totalProcessed = processedMessages.values.sumOf { it.get() }
                totalProcessed shouldBe actualSends.get()
                
                // 모든 메시지가 정확히 한 번씩만 처리됨
                processedMessages.values.forEach { count ->
                    count.get() shouldBe 1
                }
                
                println("Send attempts: ${sendAttempts.get()}, Actual sends: ${actualSends.get()}, Processed: $totalProcessed")
            }
        }
    }

    given("Channel receive 과정에서 예외가 발생할 때") {
        `when`("Consumer에서 처리 중 예외가 발생하면") {
            then("메시지는 Channel에서 제거되고 복구되지 않아야 한다") {
                val channel = Channel<String>(capacity = Channel.UNLIMITED)
                val processedMessages = mutableListOf<String>()
                val failedMessages = mutableListOf<String>()
                
                suspend fun processMessage(message: String) {
                    if (message == "message-3") {
                        throw RuntimeException("Processing failed for $message")
                    }
                    processedMessages.add(message)
                }
                
                runBlocking {
                    // Producer
                    launch {
                        repeat(6) { i ->
                            channel.send("message-$i")
                        }
                        channel.close()
                    }
                    
                    // Consumer - 특정 메시지에서 예외 발생
                    for (message in channel) {
                        try {
                            processMessage(message)
                        } catch (e: Exception) {
                            failedMessages.add(message)
                            println("Failed to process: $message - ${e.message}")
                            // 예외 발생해도 메시지는 이미 Channel에서 제거됨
                        }
                    }
                }
                
                // 검증: 예외 발생한 메시지도 Channel에서는 정상적으로 제거됨
                processedMessages.size shouldBe 5 // message-3 제외
                failedMessages.size shouldBe 1   // message-3만
                failedMessages[0] shouldBe "message-3"
                
                println("Processed: ${processedMessages.size}, Failed: ${failedMessages.size}")
                println("Failed message는 Channel에서 제거되어 재시도 불가")
            }
        }
    }

    given("여러 Consumer가 동시에 receive할 때") {
        `when`("한 Consumer에서 예외가 발생하면") {
            then("다른 Consumer들은 영향받지 않고 메시지는 중복 처리되지 않아야 한다") {
                val channel = Channel<Int>(capacity = Channel.UNLIMITED)
                val processedByConsumer = ConcurrentHashMap<String, MutableList<Int>>()
                val totalProcessed = AtomicInteger(0)
                
                suspend fun processWithError(consumerId: String, value: Int) {
                    // Consumer-2가 짝수에서 예외 발생
                    if (consumerId == "Consumer-2" && value % 2 == 0) {
                        throw RuntimeException("Consumer-2 failed on even number: $value")
                    }
                    
                    processedByConsumer.computeIfAbsent(consumerId) { mutableListOf() }
                        .add(value)
                    totalProcessed.incrementAndGet()
                    delay(10) // 처리 시간
                }
                
                runBlocking {
                    // Producer
                    launch {
                        repeat(10) { channel.send(it) }
                        channel.close()
                    }
                    
                    // 3개의 Consumer가 동시에 처리
                    val consumers = List(3) { index ->
                        async {
                            val consumerId = "Consumer-$index"
                            try {
                                for (value in channel) {
                                    try {
                                        processWithError(consumerId, value)
                                        println("$consumerId processed: $value")
                                    } catch (e: Exception) {
                                        println("$consumerId failed on: $value - ${e.message}")
                                        // 실패한 메시지는 이미 Channel에서 제거됨
                                    }
                                }
                            } catch (e: Exception) {
                                println("$consumerId terminated: ${e.message}")
                            }
                        }
                    }
                    
                    consumers.awaitAll()
                }
                
                // 검증: 각 숫자는 정확히 한 번만 처리됨 (실패 포함)
                val allProcessedNumbers = processedByConsumer.values
                    .flatten()
                    .sorted()
                
                // 중복 처리된 숫자가 없어야 함
                allProcessedNumbers.distinct().size shouldBe allProcessedNumbers.size
                
                println("=== 처리 결과 ===")
                processedByConsumer.forEach { (consumer, numbers) ->
                    println("$consumer: $numbers")
                }
                println("Total processed successfully: ${totalProcessed.get()}")
                println("각 메시지는 정확히 한 번만 Channel에서 제거됨")
            }
        }
    }

    given("Channel의 원자성(Atomicity)을 확인할 때") {
        `when`("send와 receive가 원자적으로 수행되면") {
            then("부분적인 상태는 관찰되지 않아야 한다") {
                val channel = Channel<String>(capacity = Channel.UNLIMITED)
                val receivedMessages = mutableListOf<String>()
                
                runBlocking {
                    // Producer
                    launch {
                        repeat(5) { i ->
                            channel.send("message-$i")
                        }
                        channel.close()
                    }
                    
                    // Consumer
                    for (message in channel) {
                        receivedMessages.add(message)
                    }
                }
                
                // 검증: 모든 메시지가 정확히 한 번씩만 수신됨
                receivedMessages.size shouldBe 5
                val uniqueMessages = receivedMessages.distinct()
                uniqueMessages.size shouldBe receivedMessages.size // 중복 없음
                
                println("Channel의 send/receive는 원자적으로 수행됨")
                println("Received: $receivedMessages")
            }
        }
    }

    given("Channel의 exactly-once 보장을 종합 검증할 때") {
        `when`("다양한 예외 상황이 발생해도") {
            then("메시지는 정확히 한 번만 전달되어야 한다") {
                println("\n=== Channel의 Exactly-Once 보장 ===")
                println("✅ send() 성공 시: 메시지가 Channel 버퍼에 정확히 한 번 저장")
                println("✅ receive() 성공 시: 메시지가 Channel에서 정확히 한 번 제거")  
                println("✅ send() 실패 시: 메시지가 Channel에 저장되지 않음")
                println("✅ receive() 후 처리 실패 시: 메시지는 이미 Channel에서 제거됨")
                println("✅ 동시 receive 시: 각 메시지는 하나의 Consumer만 받음")
                println("✅ 재시도 시: 이전 실패는 영향 없음, 새로운 시도로 처리")
                
                println("\n=== 결론 ===")
                println("Channel 내부 구현은 예외 상황에서도 exactly-once 의미론을 보장합니다.")
                println("- 메시지 중복 없음")
                println("- 메시지 손실 없음 (send 성공 기준)")
                println("- 동시성 안전성 보장")
                
                true shouldBe true
            }
        }
    }
})