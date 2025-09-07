# CoroutineScope 완전 이해 가이드

코루틴 스코프는 코루틴의 **실행 범위와 생명주기를 정의**하는 핵심 개념입니다.

## 1. 기본 개념: runBlocking vs coroutineScope

### runBlocking - 코루틴 진입점
```kotlin
fun main() = runBlocking {  // 일반 함수에서 사용
    delay(1000)  // 메인 스레드 블록
    println("완료")
}
```
- **스레드 블로킹**: 현재 스레드를 멈춤
- **용도**: main 함수, 테스트, 브릿지 역할
- **모든 자식 대기**: 내부 코루틴 완료까지 기다림

### coroutineScope - 구조화된 병렬 작업
```kotlin
suspend fun fetchData() = coroutineScope {  // suspend 함수 내부에서 사용
    val profile = async { fetchProfile() }
    val posts = async { fetchPosts() }
    
    UserData(profile.await(), posts.await())
}  // 모든 async 완료까지 대기
```
- **논블로킹**: 스레드를 멈추지 않음
- **용도**: 병렬 작업 조합
- **모든 자식 대기**: runBlocking과 동일하게 대기

## 2. CoroutineScope() - 독립적인 스코프 생성

### 언제 사용하는가?
- **Fire and Forget**: 결과를 기다리지 않는 백그라운드 작업
- **장기 실행**: 애플리케이션 생명주기와 같은 서비스
- **리소스 분리**: 독립적인 에러 핸들링이 필요한 경우

```kotlin
class EmailService {
    private val emailScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    fun sendEmailAsync(email: Email) {
        emailScope.launch {  // 호출자가 기다리지 않음
            sendEmail(email)
        }
    }  // 함수 즉시 반환, 이메일은 백그라운드에서 처리
    
    fun shutdown() = emailScope.cancel()  // 반드시 정리!
}
```

## 3. 핵심 차이점: 상속 vs 독립성

### 컨텍스트 상속 (coroutineScope)
```kotlin
runBlocking(CoroutineName("Parent") + Dispatchers.IO) {
    coroutineScope {
        // Job: 새로 생성 (구조적 자식)
        // CoroutineName: "Parent" (상속)
        // Dispatcher: Dispatchers.IO (상속)
    }
}
```

### 완전 독립 (CoroutineScope())
```kotlin
runBlocking(CoroutineName("Parent") + Dispatchers.IO) {
    val independent = CoroutineScope(Job() + CoroutineName("Independent"))
    independent.launch {
        // Job: 완전히 독립적 (부모 없음)
        // CoroutineName: "Independent" (지정한 값)
        // Dispatcher: Dispatchers.Default (기본값)
    }
}
```

## 4. 대기 동작의 차이

### coroutineScope는 구조적 자식만 대기
```kotlin
suspend fun example() = coroutineScope {
    // ✅ 이 코루틴은 대기함 (구조적 자식)
    launch {
        delay(100)
        println("구조적 자식 완료")
    }
    
    // ❌ 이 코루틴은 대기하지 않음 (독립적)
    val independent = CoroutineScope(Job())
    independent.launch {
        delay(200)
        println("독립적 코루틴 완료 - 나중에 출력")
    }
    
    println("coroutineScope 완료")  // 구조적 자식만 기다림
}
```

**실행 결과:**
```
구조적 자식 완료
coroutineScope 완료
독립적 코루틴 완료 - 나중에 출력  // 나중에 출력됨
```

### 메모리 누수 위험
```kotlin
suspend fun dangerousPattern() = coroutineScope {
    val backgroundScope = CoroutineScope(Job())
    backgroundScope.launch {
        while (true) {  // 무한 루프
            delay(1000)
            println("백그라운드 작업...")
        }
    }
    // coroutineScope가 끝나도 backgroundScope는 계속 실행됨!
}
```

### 안전한 패턴
```kotlin
suspend fun safePattern() = coroutineScope {
    val backgroundScope = CoroutineScope(Job())
    
    try {
        val backgroundJob = backgroundScope.launch { /* 작업 */ }
        
        // 다른 구조적 작업들...
        launch { /* 구조적 작업 */ }
        
        // 필요하다면 독립적 작업도 명시적으로 대기
        backgroundJob.join()
    } finally {
        backgroundScope.cancel()  // 반드시 정리!
    }
}
```

## 5. 예외 처리의 차이

### 스코프 함수 - 호출자에게 직접 전파
```kotlin
try {
    coroutineScope {
        launch { throw RuntimeException("예외") }
    }
} catch (e: RuntimeException) {
    println("잡힘!")  // ✅ 여기서 처리
}
```

### 코루틴 빌더 - Job 구조를 따라 전파
```kotlin
try {
    launch { throw RuntimeException("예외") }
} catch (e: RuntimeException) {
    println("잡히지 않음!")  // ❌ 여기서 잡히지 않음
}
// 예외는 부모 Job으로 전파됨
```

## 6. 실용적 사용 패턴

### 병렬 데이터 페칭 - coroutineScope
```kotlin
suspend fun loadUserDashboard(userId: String) = coroutineScope {
    val profile = async { userService.getProfile(userId) }
    val posts = async { postService.getPosts(userId) }
    val notifications = async { notificationService.getNotifications(userId) }
    
    Dashboard(
        profile = profile.await(),
        posts = posts.await(),
        notifications = notifications.await()
    )
}  // 모든 데이터 로드 완료까지 대기
```

### 백그라운드 서비스 - CoroutineScope()
```kotlin
@Service
class NotificationService {
    private val serviceScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        CoroutineName("NotificationService")
    )
    
    @PostConstruct
    fun startProcessing() {
        serviceScope.launch {
            while (isActive) {
                processNotificationQueue()
                delay(5.seconds)
            }
        }
    }
    
    @PreDestroy
    fun shutdown() = serviceScope.cancel()
}
```

## 7. 선택 기준

| 상황                                     | 사용할 방식             | 이유                        |
|----------------------------------------|--------------------|---------------------------|
| 여러 API를 동시에 호출하고 모든 결과 필요              | `coroutineScope`   | 구조화된 병렬 처리, 모든 결과 대기      |
| 이메일 발송, 로그 기록 등 백그라운드 작업               | `CoroutineScope()` | Fire-and-forget, 독립적 생명주기 |
| 에러 처리를 직접 하고 싶음                        | `coroutineScope`   | try-catch로 직접 처리 가능       |
| 서로 다른 설정(Dispatcher, ExceptionHandler) | `CoroutineScope()` | 독립적 설정 관리                 |
| 테스트 환경에서 격리된 실행                        | `coroutineScope`   | 예측 가능한 완료 시점              |

## 8. 핵심 원칙

1. **구조화된 동시성**: 부모는 모든 **구조적 자식**의 완료를 보장
2. **상속 vs 독립성**: coroutineScope는 상속, CoroutineScope()는 독립
3. **예외 전파**: 스코프 함수는 호출자에게, 빌더는 Job 구조를 따라
4. **자원 관리**: 독립적 스코프는 반드시 수동으로 cancel() 호출
5. **대기 동작**: coroutineScope는 구조적 자식만 기다림

**기억하세요**: `coroutineScope`는 구조화된 작업에, `CoroutineScope()`는 독립적인 작업에 사용하되, 반드시 적절한 정리가 필요합니다.
