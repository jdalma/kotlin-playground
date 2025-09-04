# CoroutineContext 완전 이해 가이드

CoroutineContext는 코루틴의 실행 환경과 동작을 제어하는 **여러 요소들의 조합**입니다.

## 1. CoroutineContext 구성 요소

### Job - 구조적 관계 담당
```kotlin
val job = Job()
launch(job) { } // 이 코루틴의 부모는 job
```
- **역할**: 부모-자식 관계, 예외 전파, 취소 전파
- **키**: `Job`
- **기본값**: 자동 생성되는 Job

### ContinuationInterceptor (Dispatcher) - 실행 스레드 담당
```kotlin
launch(Dispatchers.IO) { } // IO 스레드에서 실행
```
- **역할**: 어느 스레드에서 실행할지 결정
- **키**: `ContinuationInterceptor`
- **기본값**: 부모에서 상속

### CoroutineName - 디버깅용 이름
```kotlin
launch(CoroutineName("MyCoroutine")) { }
```
- **역할**: 디버깅, 로깅 시 코루틴 식별
- **키**: `CoroutineName`
- **기본값**: null

### CoroutineExceptionHandler - 예외 처리
```kotlin
val handler = CoroutineExceptionHandler { _, exception ->
    println("예외 발생: $exception")
}
launch(handler) { }
```
- **역할**: uncaught exception 처리
- **키**: `CoroutineExceptionHandler`
- **기본값**: null

## 2. 컨텍스트 결합 방식

### + 연산자로 요소 결합
```kotlin
runBlocking(Dispatchers.IO + CoroutineName("Main")) {
    // 현재 컨텍스트: Dispatchers.IO + CoroutineName("Main") + runBlocking의 Job
    
    val customJob = Job()
    launch(customJob + Dispatchers.Default) {
        // 최종 컨텍스트:
        // - Job: customJob (새로 지정)
        // - Dispatcher: Dispatchers.Default (새로 지정) 
        // - CoroutineName: "Main" (부모에서 상속)
        // - ExceptionHandler: null (부모에서 상속)
    }
}
```

## 3. 상속 vs 오버라이드 규칙

```kotlin
runBlocking(Dispatchers.IO + CoroutineName("Parent")) {
    // 부모 컨텍스트: IO + "Parent" + runBlocking-Job
    
    launch(Job() + CoroutineName("Child")) {
        // 결과 컨텍스트:
        // Job: 새 Job() ← 오버라이드
        // Dispatcher: Dispatchers.IO ← 상속  
        // Name: "Child" ← 오버라이드
        
        println(coroutineContext[Job]) // 새 Job
        println(coroutineContext[ContinuationInterceptor]) // Dispatchers.IO
        println(coroutineContext[CoroutineName]) // "Child"
    }
}
```

**규칙:**
- **명시하지 않은 요소**: 부모에서 상속
- **명시한 요소**: 새로운 값으로 오버라이드

## 4. 구조적 관계 vs 컨텍스트 상속

### 핵심 개념 분리
- **구조적 관계**: Job에 의해 결정 (예외/취소 전파 경로)
- **컨텍스트 상속**: 실행 환경 공유 (Dispatcher, Name 등)

### 실제 동작 예시
```kotlin
runBlocking(Dispatchers.Main + CoroutineName("UI")) {
    val backgroundJob = Job()
    
    launch(backgroundJob + Dispatchers.IO + CoroutineName("Background")) {
        // 이 코루틴의 최종 컨텍스트:
        // 1. Job: backgroundJob (구조적 부모)
        // 2. Dispatcher: Dispatchers.IO (실행 스레드) 
        // 3. Name: "Background" (디버깅용)
        
        throw RuntimeException() 
        // backgroundJob으로만 전파됨 (runBlocking 영향 없음)
    }
}
```

## 5. 컨텍스트 접근 방법

```kotlin
launch {
    // 현재 코루틴의 컨텍스트 요소 접근
    val currentJob = coroutineContext[Job]
    val dispatcher = coroutineContext[ContinuationInterceptor]
    val name = coroutineContext[CoroutineName]
    val handler = coroutineContext[CoroutineExceptionHandler]
    
    println("Job: $currentJob")
    println("Dispatcher: $dispatcher") 
    println("Name: $name")
    println("Handler: $handler")
}
```

## 6. 실용적인 패턴들

### 서비스별 스코프 생성
```kotlin
class UserService {
    private val serviceScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        CoroutineName("UserService") +
        CoroutineExceptionHandler { _, exception ->
            logger.error("UserService error", exception)
        }
    )
    
    fun processUser() {
        serviceScope.launch {
            // UserService 컨텍스트로 실행
        }
    }
}
```

### 테스트용 컨텍스트
```kotlin
@Test
fun testCoroutine() = runTest {
    val testJob = Job()
    
    launch(testJob + CoroutineName("TestCoroutine")) {
        // 테스트 환경에서 격리된 실행
    }
}
```

## 7. 주요 주의사항

### Job 오버라이드의 영향
```kotlin
runBlocking {
    launch(Job()) { // 새로운 Job으로 오버라이드
        throw RuntimeException() // runBlocking에 영향 없음 (구조적으로 분리됨)
    }
}
```

### Dispatcher 변경
```kotlin
runBlocking(Dispatchers.Main) {
    launch(Dispatchers.IO) { // IO 스레드에서 실행
        // 하지만 구조적으로는 runBlocking의 자식
    }
}
```

## 8. 핵심 정리

1. **CoroutineContext = 여러 요소의 맵**: Job, Dispatcher, Name, ExceptionHandler
2. **상속 원칙**: 명시하지 않으면 부모에서 상속, 명시하면 오버라이드
3. **Job의 특별함**: 구조적 관계(예외/취소 전파)를 결정
4. **Dispatcher의 역할**: 실행 스레드만 결정, 구조적 관계와 무관
5. **구조적 부모 ≠ 컨텍스트 상속원**: 서로 다른 개념

이러한 특성을 이해하면 `launch(customJob)`이 왜 컨텍스트는 상속받으면서 구조적으로는 독립적인지 명확해집니다.