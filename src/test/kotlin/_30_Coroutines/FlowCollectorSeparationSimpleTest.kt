package _30_Coroutines

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * FlowCollector와 Flow 분리의 핵심 이점 2가지를 간단히 검증
 * 
 * Kotlin Coroutine Expert 관점:
 * - 관심사 분리로 인한 명확한 책임
 * - 타입 안전성을 통한 컴파일 타임 보호
 * 
 * Unit Test Expert 관점:
 * - 실제 동작하는 간단한 예제로 이점 확인
 */
class FlowCollectorSeparationSimpleTest : BehaviorSpec({

    given("첫 번째 핵심 이점: 관심사 분리") {
        `when`("Flow는 데이터 생성, FlowCollector는 데이터 처리를 담당") {
            then("각각 독립적으로 변경하고 재사용할 수 있다") {
                
                // 데이터 생성 로직만 담당하는 Flow
                class NumberGenerator : Flow<Int> {
                    override suspend fun collect(collector: FlowCollector<Int>) {
                        repeat(5) { i ->
                            collector.emit(i + 1)
                        }
                    }
                }
                
                // 데이터 처리 로직만 담당하는 Collector
                class EvenNumberCollector : FlowCollector<Int> {
                    val evenNumbers = mutableListOf<Int>()
                    
                    override suspend fun emit(value: Int) {
                        if (value % 2 == 0) {
                            evenNumbers.add(value)
                        }
                    }
                }
                
                runBlocking {
                    val generator = NumberGenerator()
                    val collector = EvenNumberCollector()
                    
                    // Flow와 Collector를 조합
                    generator.collect(collector)
                    
                    collector.evenNumbers shouldBe listOf(2, 4)
                    
                    // 같은 Flow를 다른 Collector와 조합 가능
                    val allNumbers = mutableListOf<Int>()
                    generator.collect { value ->
                        allNumbers.add(value)
                    }
                    
                    allNumbers shouldBe listOf(1, 2, 3, 4, 5)
                }
            }
        }
    }
    
    given("두 번째 핵심 이점: 타입 안전성") {
        `when`("수신자 타입으로 emit 스코프를 제한하면") {
            then("잘못된 위치에서의 emit 호출을 컴파일 타임에 방지한다") {
                
                // FlowCollector의 확장함수로 정의하면 emit 사용 가능
                suspend fun FlowCollector<String>.emitUppercase(text: String) {
                    emit(text.uppercase()) // ✅ 확장함수 내에서 가능
                }
                
                // ✅ 올바른 사용: FlowCollector 컨텍스트에서만 emit 가능
                fun createSafeFlow(): Flow<String> = flow {
                    emit("첫 번째 값")   // ✅ 가능
                    
                    // 헬퍼 함수도 FlowCollector 확장함수로 만들면 사용 가능
                    emitUppercase("두 번째 값")
                }
                
                runBlocking {
                    val results = mutableListOf<String>()
                    
                    createSafeFlow().collect { value ->
                        results.add(value)
                    }

                    results shouldBe listOf("첫 번째 값", "두 번째 값".uppercase())
                }

                // ❌ 잘못된 사용 예시 (실제로는 컴파일 에러)
                /*
                suspend fun wrongHelper() {
                    emit("여기서도 안됨")  // ❌ 컴파일 에러!
                }
                */
            }
        }
    }
    
    given("구조적 이점의 실제 활용") {
        `when`("복잡한 데이터 처리 파이프라인을 구성할 때") {
            then("Flow와 Collector의 분리로 깔끔한 설계가 가능하다") {
                
                // 다양한 처리 방식을 가진 Collector들
                class FilteringCollector(
                    private val predicate: (String) -> Boolean,
                    private val delegate: FlowCollector<String>
                ) : FlowCollector<String> {
                    override suspend fun emit(value: String) {
                        if (predicate(value)) {
                            delegate.emit(value)
                        }
                    }
                }
                
                class TransformingCollector(
                    private val transform: (String) -> String,
                    private val delegate: FlowCollector<String>
                ) : FlowCollector<String> {
                    override suspend fun emit(value: String) {
                        delegate.emit(transform(value))
                    }
                }
                
                runBlocking {
                    val results = mutableListOf<String>()
                    val resultCollector = object : FlowCollector<String> {
                        override suspend fun emit(value: String) {
                            results.add(value)
                        }
                    }
                    
                    // Collector들을 체이닝하여 파이프라인 구성
                    val pipeline = FilteringCollector(
                        predicate = { it.length > 3 },
                        delegate = TransformingCollector(
                            transform = { it.uppercase() },
                            delegate = resultCollector
                        )
                    )
                    
                    // Flow는 단순히 데이터만 제공
                    val dataFlow = flow {
                        emit("hi")      // 필터링됨 (길이 <= 3)
                        emit("hello")   // 통과 -> "HELLO"
                        emit("bye")     // 필터링됨 (길이 <= 3) 
                        emit("world")   // 통과 -> "WORLD"
                    }
                    
                    dataFlow.collect(pipeline)
                    
                    results shouldBe listOf("HELLO", "WORLD")
                }
            }
        }
    }
})
