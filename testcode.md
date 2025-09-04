# Kotest & 코루틴 테스트 코드 작성 프롬프트

다음 요구사항에 따라 Kotest를 활용한 코틀린/코루틴 테스트 코드를 작성해주세요:

## 테스트 프레임워크 설정
- **주 프레임워크**: Kotest (5.x 이상)
- **코루틴 테스트**: kotlinx-coroutines-test
- **모킹 라이브러리**: MockK (코루틴 지원)
- **추가 라이브러리**: Kotest Extensions, Testcontainers-kotest 등

## 코드 스타일 가이드라인

### 1. 테스트 스펙 스타일 선택

#### StringSpec (간단한 테스트)
```kotlin
class UserServiceTest : StringSpec({
    "사용자 조회 시 존재하는 사용자를 반환해야 한다" {
        // given
        val userId = 1L
        
        // when
        val result = userService.findById(userId)
        
        // then
        result.shouldNotBeNull()
        result.id shouldBe userId
    }
})
```

#### FunSpec (구조화된 테스트)
```kotlin
class UserServiceTest : FunSpec({
    test("사용자 생성") {
        // 테스트 로직
    }
    
    context("사용자가 존재할 때") {
        test("정상적으로 조회되어야 한다") {
            // 테스트 로직
        }
        
        test("이메일 업데이트가 가능해야 한다") {
            // 테스트 로직
        }
    }
})
```

#### BehaviorSpec (BDD 스타일)
```kotlin
class UserServiceTest : BehaviorSpec({
    given("유효한 사용자 데이터가 주어졌을 때") {
        val userData = UserCreateRequest("홍길동", "hong@test.com")
        
        `when`("사용자를 생성하면") {
            val result = userService.create(userData)
            
            then("사용자가 성공적으로 생성되어야 한다") {
                result.shouldNotBeNull()
                result.name shouldBe "홍길동"
                result.email shouldBe "hong@test.com"
            }
        }
    }
})
```

### 2. 코루틴 테스트 패턴

#### 기본 코루틴 테스트
```kotlin
class CoroutineServiceTest : FunSpec({
    test("비동기 데이터 처리") {
        runTest {
            // given
            val mockData = listOf("data1", "data2")
            
            // when
            val result = asyncService.processData(mockData)
            
            // then
            result shouldHaveSize 2
        }
    }
})
```

#### 시간 제어가 필요한 테스트
```kotlin
test("지연된 작업 테스트") {
    runTest {
        // given
        val startTime = currentTime
        
        // when
        launch {
            delay(1000)
            // 작업 수행
        }
        
        // 시간 진행
        advanceTimeBy(1000)
        advanceUntilIdle()
        
        // then
        currentTime shouldBe startTime + 1000
    }
}
```

#### Flow 테스트
```kotlin
test("Flow 데이터 스트림 테스트") {
    runTest {
        // given
        val testData = listOf(1, 2, 3, 4, 5)
        
        // when
        val flow = dataService.getDataStream()
        
        // then
        flow.test {
            repeat(5) { index ->
                awaitItem() shouldBe testData[index]
            }
            awaitComplete()
        }
    }
}
```

### 3. MockK와 코루틴 통합

```kotlin
class ServiceTest : FunSpec({
    val mockRepository = mockk<UserRepository>()
    val userService = UserService(mockRepository)
    
    test("코루틴이 포함된 서비스 모킹") {
        runTest {
            // given
            coEvery { mockRepository.findByIdAsync(any()) } returns User(1L, "Test")
            
            // when
            val result = userService.findUserAsync(1L)
            
            // then
            result.shouldNotBeNull()
            coVerify { mockRepository.findByIdAsync(1L) }
        }
    }
})
```

### 4. 데이터 기반 테스트

```kotlin
class ValidationTest : FunSpec({
    context("이메일 유효성 검사") {
        withData(
            "valid@email.com" to true,
            "invalid.email" to false,
            "@invalid.com" to false,
            "valid@sub.domain.com" to true
        ) { (email, expected) ->
            EmailValidator.isValid(email) shouldBe expected
        }
    }
})
```

### 5. Property-based Testing

```kotlin
class MathUtilsTest : FunSpec({
    test("덧셈 연산 속성 테스트") {
        checkAll<Int, Int> { a, b ->
            val result = MathUtils.add(a, b)
            result shouldBe (a + b)
            // 교환 법칙
            MathUtils.add(a, b) shouldBe MathUtils.add(b, a)
        }
    }
})
```

### 6. 테스트 라이프사이클

```kotlin
class DatabaseTest : FunSpec({
    lateinit var database: Database
    
    beforeSpec {
        database = Database.connect()
    }
    
    afterSpec {
        database.close()
    }
    
    beforeEach {
        database.clearData()
    }
    
    test("데이터 저장 테스트") {
        // 테스트 로직
    }
})
```

## 작성 요청사항

### 테스트 대상 및 스타일
[원하는 테스트 스펙 스타일을 명시해주세요]
- StringSpec: 간단한 단위 테스트
- FunSpec: 구조화된 테스트
- BehaviorSpec: BDD 스타일 테스트
- DescribeSpec: RSpec 스타일 테스트

### 코루틴 테스트 요구사항
[다음 중 필요한 항목들을 명시해주세요]
- 기본 suspend 함수 테스트
- Flow 테스트
- 시간 기반 지연 테스트
- 병렬 처리 테스트
- 예외 처리 테스트
- 취소(Cancellation) 테스트

### 테스트 시나리오
[포함할 테스트 케이스들]
- 정상 케이스 (Happy Path)
- 경계값 및 엣지 케이스
- 예외 상황 및 오류 처리
- 성능 및 타임아웃 테스트
- 동시성 이슈 테스트

### 특별 요구사항
[필요한 경우 추가 요구사항]
- Property-based testing 포함 여부
- 데이터 기반 테스트 (withData) 사용
- 특정 MockK 기능 활용
- 테스트 컨테이너 통합
- 커스텀 매처(Matcher) 생성

### 프로젝트 컨텍스트
[기술 스택 및 아키텍처 정보]
- Kotlin 버전
- 코루틴 버전
- Spring WebFlux / Ktor 등
- 데이터베이스 (R2DBC, MongoDB 등)
- 메시지 큐 (Kafka, RabbitMQ 등)

## 예시 요청

```
UserService와 UserRepository에 대한 Kotest 기반 테스트 코드를 작성해주세요.

테스트 스타일: BehaviorSpec 사용

코루틴 기능:
- findUserAsync() 메소드 (suspend 함수)
- getUserStream() 메소드 (Flow 반환)
- batchCreateUsers() 메소드 (병렬 처리)

테스트 시나리오:
- 사용자 비동기 조회 성공/실패
- 사용자 스트림 데이터 검증
- 배치 생성 시 병렬 처리 검증
- 타임아웃 상황 처리
- 코루틴 취소 시나리오
```
---

위 가이드라인을 참고하여 Kotest의 강력한 기능들과 코루틴을 활용한 현대적이고 표현력 있는 테스트 코드를 작성해주세요.
