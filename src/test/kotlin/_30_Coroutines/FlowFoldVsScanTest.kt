package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Flow의 fold와 scan 차이점을 명확히 보여주는 테스트
 * 
 * Kotlin Coroutine Expert 관점:
 * - fold: Terminal Operation (최종 결과만)
 * - scan: Intermediate Operation (중간 과정 모두 emit)
 * - runningFold/runningReduce와의 관계
 * 
 * Unit Test Expert 관점:
 * - 실제 동작 차이를 구체적인 예제로 검증
 * - 사용 시나리오별 적절한 선택 기준 제시
 */
class FlowFoldVsScanTest : BehaviorSpec({

    given("기본적인 fold vs scan 동작") {
        `when`("숫자 합계를 구할 때") {
            then("fold는 최종 결과만, scan은 누적 과정을 모두 보여준다") {
                
                val numbers = flowOf(1, 2, 3, 4, 5)
                
                runBlocking {
                    // fold: Terminal Operation - 최종 결과만 반환
                    val foldResult = numbers.fold(0) { acc, value ->
                        println("fold - acc: $acc, value: $value, result: ${acc + value}")
                        acc + value
                    }
                    
                    // scan: Intermediate Operation - 중간 과정 모두 emit
                    val scanResults = numbers.scan(0) { acc, value ->
                        println("scan - acc: $acc, value: $value, result: ${acc + value}")
                        acc + value
                    }.toList()
                    
                    foldResult shouldBe 15  // 최종 합계만
                    scanResults shouldBe listOf(0, 1, 3, 6, 10, 15)  // 초기값 + 모든 누적값
                }
            }
        }
    }
    
    given("실시간 데이터 처리 시나리오") {
        `when`("주식 가격 변동을 추적할 때") {
            then("scan으로 실시간 포트폴리오 가치 변화를 관찰할 수 있다") {
                
                val stockPriceChanges = flow {
                    emit(+10)  // +10 상승
                    emit(-5)   // -5 하락  
                    emit(+15)  // +15 상승
                    emit(-3)   // -3 하락
                }
                
                runBlocking {
                    val initialValue = 1000
                    
                    // scan: 실시간 포트폴리오 가치 추적
                    val portfolioValues = stockPriceChanges
                        .scan(initialValue) { currentValue, change ->
                            currentValue + change
                        }
                        .toList()
                    
                    // fold: 최종 가치만
                    val finalValue = stockPriceChanges
                        .fold(initialValue) { currentValue, change ->
                            currentValue + change
                        }
                    
                    portfolioValues shouldBe listOf(1000, 1010, 1005, 1020, 1017)
                    finalValue shouldBe 1017
                    
                    println("포트폴리오 가치 변화: $portfolioValues")
                    println("최종 가치: $finalValue")
                }
            }
        }
    }
    
    given("데이터 분석 시나리오") {
        `when`("온도 센서 데이터를 분석할 때") {
            then("scan으로 이동 평균을 실시간으로 계산할 수 있다") {
                
                val temperatureReadings = flowOf(20.0, 22.0, 25.0, 23.0, 21.0, 24.0)
                
                runBlocking {
                    // scan으로 누적 평균 계산
                    data class TempStats(val sum: Double, val count: Int, val average: Double)
                    
                    val movingAverages = temperatureReadings
                        .scan(TempStats(0.0, 0, 0.0)) { stats, temp ->
                            val newSum = stats.sum + temp
                            val newCount = stats.count + 1
                            TempStats(newSum, newCount, newSum / newCount)
                        }
                        .drop(1) // 초기값 제거
                        .map { it.average }
                        .toList()
                    
                    // fold로 최종 평균만 계산
                    val finalAverage = temperatureReadings
                        .fold(TempStats(0.0, 0, 0.0)) { stats, temp ->
                            val newSum = stats.sum + temp
                            val newCount = stats.count + 1
                            TempStats(newSum, newCount, newSum / newCount)
                        }
                        .average
                    
                    // 반올림 처리로 검증
                    val roundedAverages = movingAverages.map { (it * 100).toInt() / 100.0 }
                    roundedAverages shouldBe listOf(20.0, 21.0, 22.33, 22.5, 22.2, 22.5)
                    finalAverage shouldBe 22.5
                    
                    println("이동 평균: $movingAverages")
                    println("최종 평균: $finalAverage")
                }
            }
        }
    }
    
    given("에러 처리와 복구") {
        `when`("네트워크 요청 재시도 카운터를 관리할 때") {
            then("scan으로 재시도 상태 변화를 추적할 수 있다") {
                
                val requestResults = flow {
                    emit("FAIL")    // 1차 실패
                    emit("FAIL")    // 2차 실패  
                    emit("SUCCESS") // 3차 성공
                    emit("FAIL")    // 4차 실패
                }
                
                runBlocking {
                    data class RetryState(
                        val attempts: Int,
                        val consecutiveFailures: Int,
                        val lastResult: String
                    )
                    
                    val retryStates = requestResults
                        .scan(RetryState(0, 0, "")) { state, result ->
                            RetryState(
                                attempts = state.attempts + 1,
                                consecutiveFailures = if (result == "FAIL") 
                                    state.consecutiveFailures + 1 else 0,
                                lastResult = result
                            )
                        }
                        .drop(1) // 초기값 제거
                        .toList()
                    
                    val finalState = requestResults
                        .fold(RetryState(0, 0, "")) { state, result ->
                            RetryState(
                                attempts = state.attempts + 1,
                                consecutiveFailures = if (result == "FAIL") 
                                    state.consecutiveFailures + 1 else 0,
                                lastResult = result
                            )
                        }
                    
                    retryStates.map { it.consecutiveFailures } shouldBe listOf(1, 2, 0, 1)
                    retryStates.map { it.attempts } shouldBe listOf(1, 2, 3, 4)
                    
                    finalState.attempts shouldBe 4
                    finalState.consecutiveFailures shouldBe 1
                    finalState.lastResult shouldBe "FAIL"
                }
            }
        }
    }
    
    given("성능과 메모리 고려사항") {
        `when`("대용량 데이터를 처리할 때") {
            then("fold는 메모리 효율적이고, scan은 중간 결과가 필요할 때 사용한다") {
                
                runBlocking {
                    val largeNumbers = (1..1000).asFlow()
                    
                    // fold: 메모리 효율적 - 최종 결과만 보관
                    val sum = largeNumbers.fold(0L) { acc, value ->
                        acc + value
                    }
                    
                    // scan: 모든 중간 결과를 emit하므로 메모리 사용량 많음
                    val firstTenCumulativeSums = largeNumbers
                        .scan(0L) { acc, value -> acc + value }
                        .drop(1) // 초기값 제거
                        .take(10)
                        .toList()
                    
                    sum shouldBe 500500L // 1부터 1000까지의 합
                    firstTenCumulativeSums shouldBe listOf(1L, 3L, 6L, 10L, 15L, 21L, 28L, 36L, 45L, 55L)
                    
                    println("최종 합계: $sum")
                    println("처음 10개 누적합: $firstTenCumulativeSums")
                }
            }
        }
    }
    
    given("runningFold vs scan 비교") {
        `when`("Kotlin에서 제공하는 다른 변형들과 비교하면") {
            then("runningFold는 scan과 거의 동일하게 동작한다") {
                
                val numbers = flowOf(1, 2, 3, 4)
                
                runBlocking {
                    val scanResult = numbers.scan(0) { acc, value -> acc + value }.toList()
                    val runningFoldResult = numbers.runningFold(0) { acc, value -> acc + value }.toList()
                    
                    // scan과 runningFold는 동일한 결과
                    scanResult shouldBe runningFoldResult
                    scanResult shouldBe listOf(0, 1, 3, 6, 10)
                    
                    // runningReduce는 초기값 없이 시작
                    val runningReduceResult = numbers.runningReduce { acc, value -> acc + value }.toList()
                    runningReduceResult shouldBe listOf(1, 3, 6, 10) // 초기값 없음
                }
            }
        }
    }
    
    given("실제 UI 업데이트 시나리오") {
        `when`("장바구니 총액을 실시간으로 보여줄 때") {
            then("scan으로 실시간 UI 업데이트가 가능하다") {
                
                data class CartItem(val name: String, val price: Int)
                
                val cartUpdates = flow {
                    emit(CartItem("사과", 1000))
                    emit(CartItem("바나나", 2000))  
                    emit(CartItem("오렌지", 1500))
                }
                
                runBlocking {
                    val uiUpdates = mutableListOf<String>()
                    
                    // scan: 실시간 총액 업데이트
                    cartUpdates
                        .scan(0) { total, item -> total + item.price }
                        .collect { total ->
                            uiUpdates.add("총액: ${total}원")
                        }
                    
                    // fold: 최종 총액만
                    val finalTotal = cartUpdates
                        .fold(0) { total, item -> total + item.price }
                    
                    uiUpdates shouldBe listOf(
                        "총액: 0원",      // 초기값
                        "총액: 1000원",   // 사과 추가 후
                        "총액: 3000원",   // 바나나 추가 후  
                        "총액: 4500원"    // 오렌지 추가 후
                    )
                    
                    finalTotal shouldBe 4500
                }
            }
        }
    }
})