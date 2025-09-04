package _30_Coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.*

/**
 * SupervisorJob의 실제 동작을 확인하는 간단한 데모
 */
@OptIn(DelicateCoroutinesApi::class)
class SupervisorJobDemo : StringSpec({

    "SupervisorJob 기본 동작 확인" {
        println("\n=== SupervisorJob 기본 동작 ===")
        
        runBlocking {
            val supervisorJob = SupervisorJob()
            val scope = CoroutineScope(supervisorJob)
            
            println("1. SupervisorJob 생성 완료")
            
            // 성공하는 코루틴
            val successJob = scope.launch {
                delay(100)
                println("2. 성공 작업 완료")
            }
            
            // 실패하는 코루틴
            val failJob = scope.launch {
                delay(50)
                println("3. 실패 작업 시작 - 곧 예외 발생")
                throw RuntimeException("의도적인 실패")
            }
            
            // 실패 작업 대기 및 예외 확인
            try {
                failJob.join()
            } catch (e: Exception) {
                println("4. 예외 포착: ${e.message}")
            }
            
            // 성공 작업 완료 대기
            successJob.join()
            println("5. 성공 작업 join 완료")
            
            println("6. SupervisorJob 상태 - active: ${supervisorJob.isActive}, cancelled: ${supervisorJob.isCancelled}")
            
            supervisorJob.cancel()
        }
        
        println("=== SupervisorJob 테스트 완료 ===\n")
    }

    "일반 Job과 비교" {
        println("\n=== 일반 Job 동작 ===")
        
        runBlocking {
            val regularJob = Job()
            val scope = CoroutineScope(regularJob)
            
            println("1. 일반 Job 생성 완료")
            
            // 성공하려고 했던 코루틴 (하지만 취소될 예정)
            val wouldSucceedJob = scope.launch {
                try {
                    delay(100)
                    println("2. 일반 Job: 성공 작업 완료")
                } catch (e: CancellationException) {
                    println("2. 일반 Job: 성공 작업이 취소됨")
                    throw e
                }
            }
            
            // 실패하는 코루틴
            val failJob = scope.launch {
                delay(50)
                println("3. 일반 Job: 실패 작업 시작 - 곧 예외 발생")
                throw RuntimeException("의도적인 실패")
            }
            
            // 실패 작업 대기 및 예외 확인
            try {
                failJob.join()
            } catch (e: Exception) {
                println("4. 일반 Job: 예외 포착: ${e.message}")
            }
            
            // 다른 작업도 취소되었는지 확인
            try {
                wouldSucceedJob.join()
                println("5. 일반 Job: 예상과 달리 성공 작업이 완료됨")
            } catch (e: CancellationException) {
                println("5. 일반 Job: 성공 작업도 취소됨")
            }
            
            println("6. 일반 Job 상태 - active: ${regularJob.isActive}, cancelled: ${regularJob.isCancelled}")
        }
        
        println("=== 일반 Job 테스트 완료 ===\n")
    }

    "supervisorScope 동작 확인" {
        println("\n=== supervisorScope 동작 ===")
        
        runBlocking {
            println("1. supervisorScope 시작")
            
            try {
                supervisorScope {
                    val success = async {
                        delay(100)
                        println("2. supervisorScope: 성공 작업 완료")
                        "성공 결과"
                    }
                    
                    val fail = async {
                        delay(50)
                        println("3. supervisorScope: 실패 작업 시작")
                        throw RuntimeException("supervisorScope 내 실패")
                    }
                    
                    // 실패 작업 처리
                    try {
                        fail.await()
                    } catch (e: Exception) {
                        println("4. supervisorScope: 실패 작업 예외 처리 - ${e.message}")
                    }
                    
                    // 성공 작업 완료
                    val result = success.await()
                    println("5. supervisorScope: 성공 작업 결과 - $result")
                }
            } catch (e: Exception) {
                println("supervisorScope에서 예외 발생: ${e.message}")
            }
            
            println("6. supervisorScope 완료")
        }
        
        println("=== supervisorScope 테스트 완료 ===\n")
    }

    "coroutineScope vs supervisorScope 비교" {
        println("\n=== coroutineScope vs supervisorScope ===")
        
        println("--- coroutineScope 테스트 ---")
        runBlocking {
            try {
                coroutineScope {
                    launch {
                        delay(50)
                        println("coroutineScope: 실패 작업")
                        throw RuntimeException("coroutineScope 실패")
                    }
                    
                    launch {
                        try {
                            delay(100)
                            println("coroutineScope: 성공 작업 (실행되지 않음)")
                        } catch (e: CancellationException) {
                            println("coroutineScope: 성공 작업이 취소됨")
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                println("coroutineScope에서 예외 발생: ${e.message}")
            }
        }
        
        println("--- supervisorScope 테스트 ---")
        runBlocking {
            try {
                supervisorScope {
                    val failJob = launch {
                        delay(50)
                        println("supervisorScope: 실패 작업")
                        throw RuntimeException("supervisorScope 실패")
                    }
                    
                    val successJob = launch {
                        delay(100)
                        println("supervisorScope: 성공 작업 완료")
                    }
                    
                    // 개별적으로 처리
                    try {
                        failJob.join()
                    } catch (e: Exception) {
                        println("supervisorScope: 실패 작업 예외 처리")
                    }
                    
                    successJob.join()
                }
            } catch (e: Exception) {
                println("supervisorScope에서 예외 발생: ${e.message}")
            }
        }
        
        println("=== 비교 테스트 완료 ===\n")
    }
})