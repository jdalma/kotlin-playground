# runBlocking vs launch 차이점과 코루틴 예외 처리

이 문서는 코루틴의 `runBlocking`과 `launch`의 차이점, 그리고 예외 처리 메커니즘에 대해 정리합니다.

## 1. 기본적인 차이점

### runBlocking
- **블로킹 함수**: 현재 스레드를 블록하면서 코루틴 실행
- **브릿지 역할**: 코루틴 세계와 일반 스레드 세계를 연결
- **예외 수집**: 자식 코루틴들의 예외를 수집하여 마지막에 던짐
- **완전 실행**: 취소되어도 블록 내 모든 코드를 끝까지 실행

### launch
- **논블로킹**: 새로운 코루틴을 시작하고 즉시 반환
- **Job 반환**: Job 객체를 반환하여 코루틴 제어 가능
- **즉시 취소**: 예외 발생 시 해당 코루틴 즉시 종료
- **백그라운드 실행**: 다른 작업과 동시에 실행

## 2. 예외 처리 메커니즘

### 구조화된 동시성 (Structured Concurrency)

코루틴에서는 부모-자식 관계에 따른 예외 전파 원칙이 있습니다:

```kotlin
runBlocking {  // 부모
    launch {   // 자식1 - 예외 내부 처리
        try {
            throw RuntimeException("내부 예외")
        } catch (e: Exception) {
            println("내부에서 처리됨") // ✅ 부모로 전파되지 않음
        }
    }
    
    launch {   // 자식2 - uncaught exception
        throw RuntimeException("외부로 전파될 예외") // 💥 부모로 전파됨
    }
}
```

### 예외 전파 규칙

1. **내부 catch**: 예외가 완전히 처리되면 부모로 전파되지 않음
2. **uncaught exception**: 처리되지 않은 예외는 부모로 전파됨
3. **형제 취소**: 한 자식의 예외로 인해 다른 형제 코루틴들도 취소됨
4. **CancellationException**: 정상적인 취소로 간주되어 부모로 전파되지 않음

## 3. runBlocking의 특별한 동작

### 취소되어도 끝까지 실행
```kotlin
shouldThrow<RuntimeException> {
    runBlocking {
        launch { 
            throw RuntimeException("예외 발생!") 
        } // 여기서 runBlocking이 취소됨
        
        // 하지만 아래 코드들은 모두 실행됨
        println("취소 후에도 실행됨") ✅
        delay(100) // 심지어 suspend 함수도 실행됨 ✅
        println("마지막까지 실행됨") ✅
    } // 여기서 최종적으로 예외를 외부로 던짐
}
```

### 왜 이렇게 동작하는가?

1. **완전한 정리**: 모든 자원과 상태를 정리하기 위해
2. **예외 수집**: 여러 자식에서 발생한 예외들을 모두 수집
3. **브릿지 책임**: 코루틴 세계의 모든 결과를 일반 스레드로 전달

## 4. 실제 동작 순서

```kotlin
runBlocking {  // 1. 시작
    val job1 = launch {
        try {
            throw RuntimeException("내부 처리")
        } catch (e: Exception) {
            println("job1: 내부 처리됨") // 2. 실행, 부모로 전파 안됨
        }
    }
    
    val job2 = launch {
        delay(50)
        println("job2: 정상 완료") // 3. 실행
    }
    
    joinAll(job1, job2) // 4. 두 작업 완료 대기
    
    val job3 = launch {
        throw RuntimeException("외부 전파") // 5. 💥 부모 취소 발생
    }
    
    val job4 = launch {
        delay(100) // 6. CancellationException으로 취소됨
    }
    
    try {
        job3.join() // 7. 예외 발생
    } catch (e: Exception) {
        println("예외 처리됨") // 8. ✅ runBlocking 취소되었지만 실행됨
    }
    
    println("마지막 코드") // 9. ✅ 여전히 실행됨
} // 10. 최종적으로 RuntimeException을 외부로 던짐
```

## 5. Continuation과 예외 전달

### resumeWithException vs throw

```kotlin
// ❌ 잘못된 방법 - 무한 대기 발생
suspendCoroutine { continuation ->
    GlobalScope.launch {
        try {
            throw RuntimeException("예외")
        } catch (e: Exception) {
            throw e // continuation이 resume되지 않음!
        }
    }
}

// ✅ 올바른 방법 - 상위로 예외 전달
suspendCoroutine { continuation ->
    GlobalScope.launch {
        try {
            throw RuntimeException("예외") 
        } catch (e: Exception) {
            continuation.resumeWithException(e) // 상위로 전달됨
        }
    }
}
```

### Continuation 체인에서의 예외 전달

1. **Level 1**: `continuation.resumeWithException()` 호출
2. **Level 2**: 예외 catch 후 새로운 예외로 래핑하여 재던짐  
3. **Level 3**: 다시 `continuation.resumeWithException()`으로 전달
4. **Main**: 최종적으로 try-catch에서 처리

## 6. invokeOnCompletion 활용

코루틴의 완료 상태를 모니터링할 수 있습니다:

```kotlin
val job = launch {
    throw RuntimeException("예외 발생")
}

job.invokeOnCompletion { throwable ->
    when (throwable) {
        null -> println("성공적으로 완료")
        is CancellationException -> println("취소로 완료")  
        else -> println("예외로 완료: ${throwable.message}")
    }
}
```

## 7. 핵심 원칙

1. **구조화된 동시성**: 부모는 모든 자식의 완료를 보장
2. **예외 전파**: uncaught exception은 부모로 전파됨
3. **runBlocking의 특수성**: 취소되어도 끝까지 실행
4. **Continuation의 중요성**: 반드시 resume 또는 resumeWithException 호출
5. **CancellationException**: 정상적인 취소 메커니즘

## 8. 실용적인 팁

- **예외를 완전히 처리**하고 싶다면 try-catch로 감싸기
- **부모로 예외를 전파**하고 싶다면 그냥 던지기
- **Continuation 사용 시** 반드시 resume 계열 메서드 호출
- **invokeOnCompletion**으로 완료 상태 모니터링
- **runBlocking**은 테스트나 main 함수에서만 사용

## 9. Job과 SupervisorJob의 자원 정리

### 자동 정리 vs 수동 정리

코루틴의 자원 정리는 Job의 종류와 생성 방식에 따라 다릅니다:

```kotlin
// ❌ 수동 정리 필요 - runBlocking 없이 직접 생성
val coroutineScope = CoroutineScope(Job())
coroutineScope.launch { /* 작업 */ }
coroutineScope.cancel() // 반드시 필요!

// ✅ 자동 정리 - runBlocking 사용
runBlocking {
    launch { /* 작업 */ } // runBlocking 종료 시 자동 정리
}
```

### Job 종류별 정리 동작

1. **일반 Job**
   - 자식 예외 발생 → Job 자동 취소 → 자동 정리
   - 예외 없이 완료 → 수동 cancel() 필요

2. **SupervisorJob**
   - 자식 예외 발생 → Job 계속 활성 → 수동 cancel() 필요
   - 예외 격리로 인해 자동 취소되지 않음

### 메모리 누수 방지 원칙

```kotlin
// 안전한 패턴
val scope = CoroutineScope(SupervisorJob())
try {
    scope.launch { /* 작업 */ }
    // 다른 작업들...
} finally {
    scope.cancel() // 항상 정리
}
```

**핵심**: runBlocking 밖에서 생성한 CoroutineScope는 반드시 수동으로 cancel()을 호출해야 메모리 누수를 방지할 수 있습니다.

이러한 특성들을 이해하면 코루틴의 예외 처리와 생명주기를 더 효과적으로 관리할 수 있습니다.