package _05_Exception

import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import kotlin.random.Random

open class CommonException(message: String) : Throwable(message)
open class FirstLevelException1(message: String) : CommonException(message)
class FirstLevelException2(message: String) : CommonException(message)
class SecondLevelException(message: String) : FirstLevelException1(message)

class OtherException(message: String) : Throwable(message)

fun throwException(number: Int = Random.nextInt(0, 4)) {
    when (number) {
        0 -> throw CommonException("부모 예외 클래스")
        1 -> throw FirstLevelException1("1레벨 자식 예외 클래스")
        2 -> throw SecondLevelException("2레벨 자식 예외 클래스")
        3 -> throw OtherException("그 외 예외 클래스")
    }
}

class InputException() : Throwable() {

    constructor(e: Throwable) : this() {
        Throwable(e)
    }

    constructor(message: String) : this() {
        Throwable(message)
    }
}

class StringToInt(
    val str: String,
    val bool: Boolean = true
) {
    fun throwException() = try { str.toInt() } catch (e: NumberFormatException) { throw e }
    fun returnZero() = try { str.toInt() } catch (e: NumberFormatException) { 0 }
    val throwNewException: Int = try { str.toInt() } catch (e: NumberFormatException) { throw InputException(e) }
    val result = if(bool) 0 else throw InputException()

    // 첫 번째. 예외를 변수에 대입할 때 (식의 일부분으로 사용될 때)
    val exception: Nothing = throw InputException("실행하자마자 바로 예외를 던져 프로그램이 중단된다.")

    // 두 번째. 함수 반환 타입이 Nothing인 경우
    fun function1(): Nothing {
        throw Throwable("반환 타입이 Nothing 일 때 throw 예외를 던지지 않으면 컴파일 에러")
    }
    fun function2(): Nothing {
        while(true) {
            // 무한 루프
        }
    }

}
