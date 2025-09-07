package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.select
import kotlin.time.Duration.Companion.milliseconds

/**
 * 채널 타입별 특성을 빠르게 이해하는 BDD 테스트
 * - Unlimited: 무제한 버퍼
 * - Buffered: 고정 크기 버퍼 
 * - Rendezvous: 직접 전달 (버퍼 없음)
 * - Conflated: 최신값만 유지
 */
class ChannelTypesTest : BehaviorSpec({

    given("Unlimited 채널을 사용할 때") {
        `when`("많은 데이터를 빠르게 전송하면") {
            then("모든 데이터를 버퍼에 저장해야 한다") {
                val channel = Channel<Int>(Channel.UNLIMITED)
                
                // 1000개 데이터 즉시 전송
                repeat(1000) { channel.send(it) }
                channel.close()
                
                // 모든 데이터가 순서대로 수신됨
                val received = mutableListOf<Int>()
                for (value in channel) {
                    received.add(value)
                }
                
                received.size shouldBe 1000
                received shouldBe (0..999).toList()
            }
        }
    }

    given("Buffered 채널을 사용할 때") {
        `when`("버퍼 크기를 초과하여 전송하면") {
            then("버퍼가 가득찰 때까지만 즉시 전송되어야 한다") {
                val channel = Channel<String>(capacity = 3)
                val sendResults = mutableListOf<String>()
                
                runBlocking {
                    // Producer - 버퍼 크기 초과 전송 시도
                    val producer = launch {
                        try {
                            channel.send("msg1") // 즉시 성공
                            sendResults.add("msg1 sent")
                            
                            channel.send("msg2") // 즉시 성공
                            sendResults.add("msg2 sent")
                            
                            channel.send("msg3") // 즉시 성공 (버퍼 가득)
                            sendResults.add("msg3 sent")
                            
                            channel.send("msg4") // 대기 (누군가 받을 때까지)
                            sendResults.add("msg4 sent") // 이건 나중에 실행됨
                        } finally {
                            channel.close()
                        }
                    }
                    
                    delay(100) // Producer가 먼저 실행되도록
                    
                    // 이 시점에서 msg1,2,3만 전송 완료, msg4는 대기중
                    sendResults.size shouldBe 3
                    
                    // Consumer - 하나 받으면 msg4 전송 완료됨
                    val received = channel.receive()
                    producer.join()
                    
                    received shouldBe "msg1"
                    sendResults.size shouldBe 4
                }
            }
        }
    }

    given("Rendezvous 채널을 사용할 때") {
        `when`("송신자와 수신자가 만날 때") {
            then("직접 전달되어야 한다") {
                val channel = Channel<String>() // 기본값이 Rendezvous
                var messageReceived = ""
                var senderCompleted = false
                
                runBlocking {
                    // Producer - 수신자가 없으면 대기
                    val producer = launch {
                        channel.send("rendezvous-message") // 수신자 대기
                        senderCompleted = true
                        channel.close()
                    }
                    
                    delay(50) // Producer가 대기 상태가 되도록
                    
                    // 이 시점에서 Producer는 대기중
                    senderCompleted shouldBe false
                    
                    // Consumer - 받는 순간 Producer도 완료됨
                    messageReceived = channel.receive()
                    
                    producer.join()
                    
                    // 검증: 메시지 정확히 전달되고 Producer도 완료됨
                    messageReceived shouldBe "rendezvous-message"
                    senderCompleted shouldBe true
                }
            }
        }
    }

    given("Conflated 채널을 사용할 때") {
        `when`("빠르게 여러 값을 전송하면") {
            then("최신값만 유지되어야 한다") {
                val channel = Channel<Int>(Channel.CONFLATED)
                
                runBlocking {
                    // 빠르게 5개 값 전송
                    repeat(5) { 
                        channel.send(it)
                        // delay 없이 즉시 전송 (이전 값들은 덮어씌워짐)
                    }
                    
                    // 마지막 값만 남아있음
                    val received = channel.receive()
                    received shouldBe 4
                    
                    // 채널이 비어있음 확인 (tryReceive 사용)
                    val result = channel.tryReceive()
                    val isEmpty = result.isFailure
                    isEmpty shouldBe true
                    
                    channel.close()
                }
            }
        }
        
        `when`("천천히 값을 전송하고 수신하면") {
            then("모든 값이 수신되어야 한다") {
                val channel = Channel<String>(Channel.CONFLATED)
                val received = mutableListOf<String>()
                
                runBlocking {
                    launch {
                        repeat(3) {
                            channel.send("value-$it")
                            delay(50) // 수신자가 받을 시간을 줌
                        }
                        channel.close()
                    }
                    
                    for (value in channel) {
                        received.add(value)
                        delay(30) // 천천히 수신
                    }
                }
                
                received shouldBe listOf("value-0", "value-1", "value-2")
            }
        }
    }
})
