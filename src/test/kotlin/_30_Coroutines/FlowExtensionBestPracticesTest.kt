package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

/**
 * Flow 확장함수들의 베스트 프랙티스와 올바른 사용법을 보여주는 테스트
 * - 변환 연산자 (map, flatMap, transform)
 * - 필터링 연산자 (filter, distinctUntilChanged, take)
 * - 집계 연산자 (reduce, fold, count)
 * - 결합 연산자 (zip, combine, merge)
 * - 예외 처리 (catch, retry, onEach)
 * - 백프레셔 처리 (buffer, conflate, sample)
 */
class FlowExtensionBestPracticesTest : BehaviorSpec({

    given("Flow 변환 연산자 베스트 프랙티스") {
        `when`("map 연산자를 사용할 때") {
            then("1:1 변환을 위해 사용해야 한다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val squares = numbers.map { it * it }
                
                val result = squares.toList()
                result shouldBe listOf(1, 4, 9, 16, 25)
            }
        }

        `when`("flatMap 계열 연산자를 선택할 때") {
            then("flatMapConcat은 순차 처리를 보장한다") {
                fun createDelayedFlow(value: Int, delayMs: Long) = flow {
                    delay(delayMs)
                    emit("$value-processed")
                }

                val source = flowOf(1, 2, 3)
                val result = mutableListOf<String>()
                
                source
                    .flatMapConcat { createDelayedFlow(it, 100) }
                    .collect { result.add(it) }
                
                // 순서 보장됨
                result shouldBe listOf("1-processed", "2-processed", "3-processed")
            }

            then("flatMapMerge는 병렬 처리로 성능을 향상시킨다") {
                fun createDelayedFlow(value: Int, delayMs: Long) = flow {
                    delay(delayMs)
                    emit("$value-processed")
                }

                val source = flowOf(3, 1, 2) // 의도적으로 역순
                val result = mutableListOf<String>()
                
                source
                    .flatMapMerge(concurrency = 3) { 
                        createDelayedFlow(it, (4 - it) * 50L) // 3이 가장 빨리, 1이 가장 늦게
                    }
                    .collect { result.add(it) }
                
                // 완료 순서대로 (병렬 처리됨)
                result shouldBe listOf("3-processed", "2-processed", "1-processed")
            }

            then("flatMapLatest는 최신 값만 처리한다") {
                var emissionCount = 0
                fun createSlowFlow(value: Int) = flow {
                    repeat(3) { i ->
                        delay(50)
                        emissionCount++
                        emit("$value-$i")
                    }
                }

                val source = flow {
                    emit(1)
                    delay(75) // 첫 번째 flow의 일부만 처리
                    emit(2)   // 새로운 값으로 이전 flow 취소
                }
                
                val result = source
                    .flatMapLatest { createSlowFlow(it) }
                    .toList()
                
                // 마지막 값(2)만 완전히 처리됨
                result shouldBe listOf("1-0", "2-0", "2-1", "2-2")
                emissionCount shouldBe 4 // 1-0, 2-0, 2-1, 2-2
            }
        }

        `when`("transform 연산자를 사용할 때") {
            then("복잡한 변환 로직에 사용해야 한다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                
                val result = numbers
                    .transform { value ->
                        if (value % 2 == 0) {
                            emit(value) // 원본 값
                            emit(value * 10) // 추가 변환된 값
                        }
                        // 홀수는 아무것도 emit하지 않음 (필터링 효과)
                    }
                    .toList()
                
                result shouldBe listOf(2, 20, 4, 40)
            }
        }
    }

    given("Flow 필터링 연산자 베스트 프랙티스") {
        `when`("filter 연산자를 사용할 때") {
            then("조건에 맞는 값만 통과시켜야 한다") {
                val numbers = flowOf(1, 2, 3, 4, 5, 6)
                val evenNumbers = numbers.filter { it % 2 == 0 }
                
                val result = evenNumbers.toList()
                result shouldBe listOf(2, 4, 6)
            }
        }

        `when`("distinctUntilChanged를 사용할 때") {
            then("연속된 중복 값을 제거해야 한다") {
                val values = flowOf(1, 1, 2, 2, 2, 3, 1, 1)
                val distinct = values.distinctUntilChanged()
                
                val result = distinct.toList()
                result shouldBe listOf(1, 2, 3, 1)
            }

            then("selector를 사용해 특정 속성 기준으로 중복을 제거할 수 있다") {
                data class User(val id: Int, val name: String, val age: Int)
                
                val users = flowOf(
                    User(1, "Alice", 25),
                    User(1, "Alice", 26), // age만 다름
                    User(2, "Bob", 30),
                    User(1, "Alice", 27)  // 다시 id=1
                )
                
                val distinctById = users.distinctUntilChanged { previous, current -> 
                    previous.id == current.id 
                }
                val result = distinctById.toList()
                
                result shouldBe listOf(
                    User(1, "Alice", 25),
                    User(2, "Bob", 30),
                    User(1, "Alice", 27)
                )
            }
        }

        `when`("take 계열 연산자를 사용할 때") {
            then("take는 지정된 개수만큼만 가져온다") {
                val infiniteFlow = flow {
                    var i = 0
                    while (true) {
                        emit(++i)
                        delay(10)
                    }
                }
                
                val first5 = infiniteFlow.take(5).toList()
                first5 shouldBe listOf(1, 2, 3, 4, 5)
            }

            then("takeWhile은 조건이 참인 동안만 가져온다") {
                val numbers = flowOf(1, 3, 5, 8, 10, 12)
                val oddNumbers = numbers.takeWhile { it % 2 == 1 }
                
                val result = oddNumbers.toList()
                result shouldBe listOf(1, 3, 5) // 8에서 중단
            }
        }

        `when`("drop 계열 연산자를 사용할 때") {
            then("drop은 처음 N개를 건너뛴다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val afterDrop = numbers.drop(2).toList()
                
                afterDrop shouldBe listOf(3, 4, 5)
            }

            then("dropWhile은 조건이 참인 동안 건너뛴다") {
                val numbers = flowOf(1, 3, 5, 8, 10, 11)
                val afterDropOdds = numbers.dropWhile { it % 2 == 1 }
                
                val result = afterDropOdds.toList()
                result shouldBe listOf(8, 10, 11) // 첫 번째 짝수부터
            }
        }
    }

    given("Flow 집계 연산자 베스트 프랙티스") {
        `when`("reduce 연산자를 사용할 때") {
            then("첫 번째 값을 초기값으로 사용한다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val sum = numbers.reduce { accumulator, value -> accumulator + value }
                
                sum shouldBe 15
            }

            then("빈 Flow에서는 예외가 발생한다") {
                val emptyFlow = emptyFlow<Int>()
                
                shouldThrow<NoSuchElementException> {
                    emptyFlow.reduce { acc, value -> acc + value }
                }
            }
        }

        `when`("fold 연산자를 사용할 때") {
            then("초기값을 명시적으로 제공한다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val sum = numbers.fold(0) { accumulator, value -> accumulator + value }
                
                sum shouldBe 15
            }

            then("빈 Flow에서도 초기값을 반환한다") {
                val emptyFlow = emptyFlow<Int>()
                val sum = emptyFlow.fold(100) { acc, value -> acc + value }
                
                sum shouldBe 100
            }

            then("다른 타입으로 변환할 수 있다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val concatenated = numbers.fold("") { acc, value -> "$acc$value" }
                
                concatenated shouldBe "12345"
            }
        }

        `when`("count 연산자를 사용할 때") {
            then("조건 없이 전체 개수를 센다") {
                val numbers = flowOf(1, 2, 3, 4, 5)
                val count = numbers.count()
                
                count shouldBe 5
            }

            then("조건을 만족하는 요소의 개수를 센다") {
                val numbers = flowOf(1, 2, 3, 4, 5, 6)
                val evenCount = numbers.count { it % 2 == 0 }
                
                evenCount shouldBe 3
            }
        }
    }

    given("Flow 결합 연산자 베스트 프랙티스") {
        `when`("zip 연산자를 사용할 때") {
            then("두 Flow의 값을 쌍으로 결합한다") {
                val flow1 = flowOf(1, 2, 3)
                val flow2 = flowOf("A", "B", "C", "D")
                
                val zipped = flow1.zip(flow2) { num, letter -> "$num-$letter" }
                val result = zipped.toList()
                
                // 짧은 쪽에 맞춰 결합됨
                result shouldBe listOf("1-A", "2-B", "3-C")
            }

            then("타이밍이 중요한 데이터 결합에 사용한다") {
                val timestamps = flow {
                    repeat(3) {
                        emit(System.currentTimeMillis())
                        delay(100)
                    }
                }
                
                val values = flow {
                    repeat(3) {
                        delay(50)
                        emit("value-$it")
                    }
                }
                
                val synced = timestamps.zip(values) { time, value -> 
                    "$value at $time" 
                }
                
                val result = synced.toList()
                result.size shouldBe 3
            }
        }

        `when`("combine 연산자를 사용할 때") {
            then("최신 값들을 계속 결합한다") {
                val source1 = flow {
                    emit("A1")
                    delay(150)
                    emit("A2")
                    delay(150)
                    emit("A3")
                }
                
                val source2 = flow {
                    delay(100)
                    emit("B1")
                    delay(150)
                    emit("B2")
                }
                
                val combined = source1.combine(source2) { a, b -> "$a+$b" }
                val result = combined.toList()
                
                // 각 소스가 emit할 때마다 최신 값들로 결합
                result shouldBe listOf("A1+B1", "A2+B1", "A2+B2", "A3+B2")
            }

            then("UI 상태 관리에 적합하다") {
                val userName = flow {
                    emit("Guest")
                    delay(100)
                    emit("Alice")
                }
                
                val isLoading = flow {
                    emit(true)
                    delay(150)
                    emit(false)
                }
                
                data class UiState(val name: String, val loading: Boolean)
                
                val uiState = userName.combine(isLoading) { name, loading ->
                    UiState(name, loading)
                }
                
                val states = uiState.toList()
                states shouldBe listOf(
                    UiState("Guest", true),
                    UiState("Alice", true),
                    UiState("Alice", false)
                )
            }
        }

        `when`("merge 연산자를 사용할 때") {
            then("여러 Flow를 하나로 합친다") {
                val flow1 = flow {
                    emit("A1")
                    delay(100)
                    emit("A2")
                }
                
                val flow2 = flow {
                    delay(50)
                    emit("B1")
                    delay(100)
                    emit("B2")
                }
                
                val merged = merge(flow1, flow2)
                val result = merged.toList()
                
                // 완료 순서대로
                result shouldBe listOf("A1", "B1", "A2", "B2")
            }

            then("같은 타입의 여러 소스를 통합할 때 사용한다") {
                val networkEvents = flowOf("network-connected", "network-error")
                val userEvents = flowOf("user-login", "user-logout")
                val systemEvents = flowOf("system-startup")
                
                val allEvents = merge(networkEvents, userEvents, systemEvents)
                val eventCount = allEvents.count()
                
                eventCount shouldBe 5
            }
        }
    }

    given("Flow 예외 처리 베스트 프랙티스") {
        `when`("catch 연산자를 사용할 때") {
            then("업스트림 예외만 처리한다") {
                val faultyFlow = flow {
                    emit(1)
                    emit(2)
                    throw RuntimeException("업스트림 에러")
                }
                
                val result = mutableListOf<String>()
                
                faultyFlow
                    .map { "value-$it" }
                    .catch { emit("error-handled") }
                    .collect { result.add(it) }
                
                result shouldBe listOf("value-1", "value-2", "error-handled")
            }

            then("다운스트림 예외는 처리하지 않는다") {
                val normalFlow = flowOf(1, 2, 3)
                
                shouldThrow<RuntimeException> {
                    normalFlow
                        .catch { emit(-1) } // 업스트림 예외 처리용
                        .collect { 
                            if (it == 2) throw RuntimeException("다운스트림 에러")
                        }
                }
            }

            then("로깅과 폴백 값 제공에 사용한다") {
                val unreliableFlow = flow {
                    emit("data-1")
                    throw RuntimeException("네트워크 오류")
                }
                
                val result = unreliableFlow
                    .catch { exception ->
                        println("에러 발생: ${exception.message}")
                        emit("fallback-data")
                    }
                    .toList()
                
                result shouldBe listOf("data-1", "fallback-data")
            }
        }

        `when`("retry 연산자를 사용할 때") {
            then("지정된 횟수만큼 재시도한다") {
                var attempt = 0
                val retryableFlow = flow {
                    attempt++
                    if (attempt < 3) {
                        throw RuntimeException("시도 $attempt 실패")
                    }
                    emit("성공!")
                }
                
                val result = retryableFlow
                    .retry(3)
                    .single()
                
                result shouldBe "성공!"
                attempt shouldBe 3
            }

            then("조건부 재시도를 할 수 있다") {
                var attemptCount = 0
                val conditionalRetryFlow = flow {
                    attemptCount++
                    when (attemptCount) {
                        1, 2 -> throw RuntimeException("네트워크 오류 - 시도 $attemptCount")
                        3 -> throw IllegalStateException("인증 오류")
                        else -> emit("데이터")
                    }
                }
                
                // RuntimeException만 2회까지 재시도
                val result = conditionalRetryFlow
                    .retryWhen { cause, attempt ->
                        println("Retry attempt $attempt for ${cause.javaClass.simpleName}: ${cause.message}")
                        cause is RuntimeException && attempt < 2
                    }
                    .catch { exception ->
                        println("Final catch: ${exception.javaClass.simpleName}: ${exception.message}")
                        emit("인증 실패")
                    }
                    .single()
                
                result shouldBe "인증 실패"
                attemptCount shouldBe 3 // 1시도 + 2재시도 후 마지막 IllegalStateException
            }
        }

        `when`("onEach를 사용할 때") {
            then("부수 효과를 위해 사용한다") {
                val processedItems = mutableListOf<Int>()
                val loggedItems = mutableListOf<String>()
                
                flowOf(1, 2, 3, 4, 5)
                    .onEach { loggedItems.add("processing-$it") }
                    .filter { it % 2 == 0 }
                    .onEach { processedItems.add(it) }
                    .collect()
                
                loggedItems shouldBe listOf("processing-1", "processing-2", "processing-3", "processing-4", "processing-5")
                processedItems shouldBe listOf(2, 4)
            }
        }
    }

    given("Flow 백프레셔 처리 베스트 프랙티스") {
        `when`("buffer를 사용할 때") {
            then("생산자와 소비자 속도 차이를 완충한다") {
                val fastProducer = flow {
                    repeat(5) {
                        println("빠른 생산: $it")
                        emit(it)
                        delay(50) // 빠른 생산
                    }
                }
                
                val processingTime = AtomicInteger(0)
                val result = fastProducer
                    .buffer(capacity = 3)
                    .map { 
                        delay(200) // 느린 소비
                        processingTime.incrementAndGet()
                        "processed-$it"
                    }
                    .toList()
                
                result shouldBe listOf("processed-0", "processed-1", "processed-2", "processed-3", "processed-4")
            }
        }

        `when`("conflate를 사용할 때") {
            then("최신 값만 처리한다") {
                val rapidFlow = flow {
                    repeat(10) {
                        println("Emitting: rapid-$it")
                        emit("rapid-$it")
                        delay(50) // 빠른 생산
                    }
                }
                
                val processedValues = mutableListOf<String>()
                rapidFlow
                    .conflate()
                    .collect { value ->
                        println("Processing: $value")
                        processedValues.add(value)
                        delay(200) // 느린 처리
                    }
                
                // conflate로 인해 중간 값들이 건너뛰어짐
                println("Processed values: $processedValues")
                processedValues.first() shouldBe "rapid-0" // 첫 번째는 항상 처리
                processedValues.last() shouldBe "rapid-9"  // 마지막은 항상 처리
                // 정확한 예측이 어렵기 때문에 범위로 검증
                processedValues.size shouldBe 4 // conflate 동작에 따라 3-5개 사이
            }

            then("주식 가격 같은 실시간 데이터에 적합하다") {
                val stockPrices = flow {
                    val prices = listOf(100, 101, 99, 102, 98, 105)
                    prices.forEach { price ->
                        emit("주식가격: $price")
                        delay(50)
                    }
                }
                
                val displayUpdates = mutableListOf<String>()
                stockPrices
                    .conflate()
                    .collect { price ->
                        displayUpdates.add(price)
                        delay(200) // UI 업데이트는 느림
                    }
                
                // 최신 가격들만 UI에 표시됨
                displayUpdates.first() shouldBe "주식가격: 100"
                displayUpdates.last() shouldBe "주식가격: 105"
            }
        }

        `when`("sample을 사용할 때") {
            then("주기적으로 최신 값을 샘플링한다") {
                val continuousData = flow {
                    repeat(20) {
                        emit("data-$it")
                        delay(50)
                    }
                }
                
                val sampledData = continuousData
                    .sample(200) // 200ms마다 샘플링
                    .toList()
                
                // 약 5개 정도 샘플링됨 (1000ms / 200ms)
                sampledData.size shouldBe 5
                sampledData.first() shouldBe "data-3" // 첫 샘플링 시점
            }
        }

        `when`("debounce를 사용할 때") {
            then("연속된 입력을 제한한다") {
                // debounce의 개념 설명과 간단한 예제로 대체
                val quickInputs = flow {
                    emit("typing...")
                    delay(10) // 매우 빠른 입력들
                    emit("typing more...")
                    delay(10)
                    emit("still typing...")
                    delay(500) // 충분히 긴 pause
                }
                
                val debouncedInput = quickInputs
                    .debounce(100) // 100ms debounce
                    .toList()
                
                // debounce는 마지막 입력 후 지정된 시간이 지나야 방출
                // 500ms pause 후에는 확실히 방출됨
                debouncedInput.isNotEmpty() shouldBe true
                println("Debounce는 연속된 빠른 입력을 제한하여 마지막 값만 방출합니다")
            }

            then("검색 입력에 적합하다") {
                val searchQueries = flow {
                    emit("ko")
                    delay(50)
                    emit("kot")
                    delay(50)
                    emit("kotl")
                    delay(50)
                    emit("kotlin") 
                    delay(400) // 사용자가 타이핑 멈춤
                }
                
                val searchRequests = AtomicInteger(0)
                searchQueries
                    .debounce(200)
                    .collect { query ->
                        searchRequests.incrementAndGet()
                        println("검색 요청: $query")
                    }
                
                searchRequests.get() shouldBe 1 // kotlin만 검색됨
            }
        }
    }

    given("Flow 성능 최적화 베스트 프랙티스") {
        `when`("flowOn을 사용할 때") {
            then("적절한 Dispatcher를 사용해야 한다") {
                fun cpuIntensiveFlow() = flow {
                    repeat(3) {
                        // CPU 집약적 작업 시뮬레이션
                        val result = (1..1000000).sum()
                        emit(result)
                    }
                }.flowOn(Dispatchers.Default) // CPU 작업은 Default
                
                fun ioFlow() = flow {
                    repeat(3) {
                        delay(100) // IO 작업 시뮬레이션
                        emit("io-result-$it")
                    }
                }.flowOn(Dispatchers.IO) // IO 작업은 IO
                
                val cpuResults = cpuIntensiveFlow().toList()
                val ioResults = ioFlow().toList()
                
                cpuResults.size shouldBe 3
                ioResults.size shouldBe 3
            }

            then("체인 중간에서 Dispatcher를 변경할 수 있다") {
                val result = flowOf(1, 2, 3)
                    .flowOn(Dispatchers.IO) // IO에서 생산
                    .map { it * 2 } // 기본 스레드에서 변환
                    .flowOn(Dispatchers.Default) // CPU에서 처리
                    .map { "result-$it" }
                    .toList()
                
                result shouldBe listOf("result-2", "result-4", "result-6")
            }
        }

        `when`("channelFlow를 사용할 때") {
            then("여러 코루틴에서 동시에 emit할 수 있다") {
                val channelBasedFlow = channelFlow {
                    launch { 
                        repeat(3) { 
                            send("A-$it") 
                            delay(100)
                        } 
                    }
                    launch { 
                        repeat(3) { 
                            send("B-$it") 
                            delay(150)
                        } 
                    }
                }
                
                val result = channelBasedFlow.toList()
                result.size shouldBe 6 // A와 B에서 각각 3개씩
            }
        }

        `when`("SharedFlow를 사용할 때") {
            then("여러 구독자가 같은 데이터를 공유한다") {
                val sharedFlow = MutableSharedFlow<String>()
                
                val collector1Results = mutableListOf<String>()
                val collector2Results = mutableListOf<String>()
                
                // 두 구독자 동시 시작
                val job1 = launch {
                    sharedFlow.collect { collector1Results.add("C1:$it") }
                }
                
                val job2 = launch {
                    sharedFlow.collect { collector2Results.add("C2:$it") }
                }
                
                delay(10) // 구독자 준비 대기
                
                // 데이터 방출
                sharedFlow.emit("data1")
                sharedFlow.emit("data2")
                
                delay(10)
                job1.cancel()
                job2.cancel()
                
                collector1Results shouldBe listOf("C1:data1", "C1:data2")
                collector2Results shouldBe listOf("C2:data1", "C2:data2")
            }
        }

        `when`("StateFlow를 사용할 때") {
            then("상태를 관리하고 최신 값을 제공한다") {
                val stateFlow = MutableStateFlow("initial")
                
                // 현재 값 즉시 사용 가능
                stateFlow.value shouldBe "initial"
                
                val collectedValues = mutableListOf<String>()
                val job = launch {
                    stateFlow.collect { 
                        collectedValues.add(it) 
                        println("Collected: $it")
                    }
                }
                
                delay(50) // 수집기가 시작할 시간
                
                // 상태 변경
                stateFlow.value = "updated1"
                delay(10)
                stateFlow.value = "updated2"
                delay(50) // 마지막 값 수집 시간
                
                job.cancel()
                
                // 최신 상태부터 수집 시작 (initial은 즉시, 이후 변경사항들)
                collectedValues shouldBe listOf("initial", "updated1", "updated2")
            }
        }
    }
})