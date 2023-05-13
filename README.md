
# 무지 목록

1. `reified`
2. `vtable`
3. 코루틴 컨텍스트를 지정하기 위해 사용되는 `@Context`
   - [Context Receivers](https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md)
   - [Coroutines](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md)
4. **람다 팩토리 구조**
5. `invoke`와 `invoke dynamic` 차이점

# 1회차 모임 `2023.05.10`

1. `for`문의 `1..10`의 `Range`객체는 컴파일러의 최적화를 거치기 때문에 안심하고 써도 된다
2. 코틀린의 `do while`문은 `do` 스코프 내부에서 변수를 선언해서 `while`에 사용할 수 있다는 것이 굉장한 장점이다.
3. 참조 배열과 원시 배열의 차이점 : `Array<Int>`와 `IntArray`는 언제 구분해서 사용해하는지?
   - 저자는 제네릭의 효과를 사용하지 않을 때는 원시 배열을 사용하는게 훨씬 이득일 것이다. 
4. `null is T`로 해당 변수의 타입이 널 허용성을 구분할 수 있다!
   - ```kotlin
     inline fun <reified T> T.isNullable(): Boolean = null is T
     ```
5. `Array.indices`를 `for`문에 직접 넣어주면 오버헤드가 없다.
6. 코틀린의 타입 추론은 공짜가 아니다!
7. 자바의 람다는 인터페이스의 익명 객체지 함수 타입의 객체가 아니다.
   - 결론은 클래스 타입의 구현체다.
   - 코틀린의 람다는 타입 자체가 함수 타입이다.

# **코틀린은 왜 if와 when 식을 도입했을까?**    
(값을 만들 때 사용하는 if를 **if 식**이라고 한다.)    
if/when을 식으로 사용할 수 없으면 조건에 따라 값이 달라지는 경우 반드시 var변수를 사용하고 적당한 초깃값으로 그 변수를 초기화해야만 한다.    

```kotlin
var tmp = ""
if (doSoemthing())
    tmp = "성공"
else
    tmp = "실패"
```

`val` 사용을 편리하게 해주고 더 장려하기 위함이다.  

# **식과 연산자**

**식**  
- 어떤 한 가지 값으로 계산될 수 있는 프로그램 조각을 뜻한다.
- 가장 단순한 식은 값을 표현하는 `리터럴`이며, **식을 서로 엮어서 새로운 식을 만들때는 연산자를 사용**한다.
  - 사칙연산, 나머지연산, 비교연산, 논리연산, 함수호출
- 위에서 본 `if/when`도 식으로 사용할 수 있지만, `try/catch/finally`도 식으로 사용할 수 있다.
- 식을 이루는 연산자에는 **우선순위**와 **결합 방향**이 있다.
  - 코틀린에서 걸합 방향이 오른쪽 우선인 연산자는 `단항 부호 연산자`와 `논리 부정 연산자`뿐이다.
  
**문**
- 값을 만들어내지는 않지만 프로그램의 흐름을 제어하는 역할을 한다.
- 식이 만들어내는 값을 무시하면 식도 문 역할을 할 수 있으며, 이런 경우가 함수를 호출하고 결괏값을 사용하지 않는 경우다.
  - 이 경우는 결괏값을 돌려받아 사용하기 보다는 어떤 공유된 상태를 변경하거나 입출력을 수행하는 함수는 **사용자가 정의한 문**역할을 한다고 볼 수 있다.
- `val/var`을 선언하는 문장이나 함수, 클래스, 객체를 선언하는 문장, `if/when`문, 대입문이나 복합 대입문(+=, -=, *=, /=), 루프문, `try/catch`등이 코틀린에 사용되는 문장들이다.

# **람다식**

- **배열 생성자에 람다식 전달하기**
  - 람다식의 `it`에는 채워 넣을 객체의 인덱스가 자동으로 전달된다.

```kotlin
/**
 * init 함수는 첫 번째 배열 요소부터 순차적으로 시작하여 각 배열 요소에 대해 호출됩니다. 인덱스가 지정된 배열 요소의 값을 반환해야 합니다.
 **/
public inline constructor(size: Int, init: (Int) -> T)

val arrayOf1To10 = Array(10) {it + 1}
```

# **코틀린에서의 원시 타입은?**

- 자바의 원시 타입들은 메모리 상에서 해당 타입의 값을 표현하는 2진수 값으로 존재하고, 멤버 함수 호출이 가능한 객체로 존재하지 않는다.
- 하지만 **코틀린에서는 모든 대상을 객체처럼 취급할 수 있으며, 문맥에 따라 컴파일러가 알아서 원시 타입 값으로 처리하거나 객체로 처리해준다.**
- 이때, **`Array<Int>` 타입의 배열을 생성할 때는 원시 타입의 32비트 정수 값만 저장될지 보장할 수 없다.**
  - 이런 경우 컴파일러는 안전한 방식으로 배열의 원소들을 32비트 정수 값이 아니라 **정수 값 객체를 가리키는 참조를 사용할 수 밖에 없는 참조 배열을 사용한다.**
  - 원시 타입으로 쓸 것이 명확하다면 원시 배열의 `{Type}Array` 클래스들이 존재한다.
- [추가 내용 - kotlinlang 기본타입](https://kotlinlang.org/docs/basic-types.html)

# **익명 함수와 람다** 📌

- 익명 함수는 이름이 없는 함수 이며, 코틀린 람다는 익명 함수와 비슷하지만 좀 더 간결한 문법을 제공한다.
- **익명 함수와 람다 모두 함수 역할을 할 수 있는 값을 정의하는 `리터럴`이다.**
- 따라서 다른 타입의 리터럴과 마찬가지로 **값이나 식이 쓰일 수 있는 위치에 사용할 수 있다.**
- **람다는 항상 본문이 `식`인 형태로만 정의해야 하며, `return`을 쓸 수 없고 맨 마지막에 쓴 식의 값이 전체 값이 된다.**
- 람다는 일반적인 값과 마찬가지로 쓰일 수 있다. 
- 람다를 **데이터 구조에 저장하거나, 함수의 인자로 전달하거나, 함수에서 생성해 반환할 수 있다.** 이와 같은 성질을 **<span style="background-color:#FFE6E6">일급 시민</span>으로 취급한다.** 라고 말하며, 
- 람다를 **파라미터로 받거나 람다를 반환하는 함수를 <span style="background-color:#FFE6E6">고차 함수</span>** 라고 부른다.

```kotlin
fun andThen(
    f: (Int) -> Int,
    g: (Int) -> Int
) : (Int) -> Int = {
    value: Int -> g(f(value))
}

val called = andThen(
    { x: Int -> x + 2 },
    { x: Int -> x * x }
)(4)
```

`andThen()`의 결과 타입은 `(Int) -> Int`이다.  
Int 하나를 파라미터로 받아서 Int 타입의 결과를 내어주는 함수이다.  
`andThen` 함수의 파라미터 람다 2개로 `value`의 값을 가공하는 것인데, `value`는 `andThen`의 리턴 람다의 파라미터이다. 📌  

**컴파일러가 익명 함수나 람다의 타입을 명확히 알 수 있는 경우, 파라미터 타입을 생략해도 좋다.**  
- 익명 함수에서는 반환 타입을 지정해주지 않으면 `Unit`으로 해석한다.
그리고 람다 파라미터가 하나일 경우 `it`키워드를 사용하여 파라미터를 사용할 수 있다.  

## **클로저와 값 포획**

람다나 익명 함수도 구문적 영역 규칙을 통해 찾을 수 있는 이름을 자유롭게 사용할 수 있다.  
하지만 함수가 다른 함수를 반환하는 **고차 함수**라면 복잡한 문제가 생길 수 있다. 📌  
**고차 함수가 반환하는 `람다의 수명이 고차 함수가 실행되고 반환되는 시점 이후에도 계속 될 수 있기 때문`이다.**  
  
[클로저 예제](https://github.com/jdalma/kotlin-core-programming/commit/d6972ef64c43925fba43f35aa87e6c5c7ad524c4)에서 `state` 함수의 내부 `memory` 변수는 반환되는 `func`에 의해 참조당해야 하기 때문에 **힙 영역**에 따로 저장된다.  
이와 같이 **람다 코드와 더불어 람다를 계산할 때 필요한 모든 환경을 갖춰서 일종의 닫힌 식을 만들어주는 이런 구조를 `클로저`라고 부른다.**  
- 클로저라는 말을 익명 함수나 람다와의 동의어로 여기는 사람도 있다.
  
람다나 함수 내부에 정의되지 않은 바깥 영역에서 정의를 찾아야 하는 변수를 **자유 변수**라고 하며, 외부 영역의 변수를 클로저에 붙들게 되는 현상을 **포획**이라 한다.  
기억이 잘 나지 않는다면 [고차함수 예제](https://github.com/jdalma/kotlin-core-programming/commit/0417b9c1fc1d40ad01b8fb11871bcca57e10d2f5)를 확인하자.  
  
> 함수가 상태를 계속 유지할 수 있다는 것이 신기하다. 이 람다의 참조가 유지되는 한 상태는 계속 유지되겠지?  
> 그리고 람다 밖의 변수도 참조가 유지되어야 하기 때문에 힙 영역에 저장하는것도 신기하다.

# **클래스에 대해**

클래스 인스턴스에 대해 적용할 수 있는 함수를 **멤버 함수**라 하며, 자바 등 다른 객체지향 언어에서는 **메서드**라고도 한다.  
만약 `Score` 클래스에 `sum()`이라는 멤버 함수가 호출된다면 **함수 안에서는 자신이 호출된 대상 객체가 존재**한다. (**수신 객체**라고도 한다.)  
  
**상속**  
`~는 ~이다` 관계를 `is-a` 관계라고 한다.  
**클래스 또는 멤버**에`final` 키워드를 (작성한 것과 안 한것의 차이는 없지만) 직잡 명시하여 **상속을 막으려는 의도를 명확히 드러낼 수도 있다.**  
  
**인터페이스 오버라이드 규칙**  
동일한 시그니처를 가진 인터페이스를 여러 개 구현해야할 상황이라면 구현체는 해당 함수를 무조건 직접 재정의하여야 한다.  
  
> **동적 디스패치란?**  
> 실행 시점에 실제 참조되는 객체의 구체적인 타입에 따라 오버라이드된 적절한 멤버 함수가 호출되도록 컴파일이 이루어지는 것

# **뒷받침하는 필드와 뒷받침하는 프로퍼티**

- 클래스 내부 필드의 `setter`를 직접 정의해줬을 때, `init block`으로 필드를 초기화하면 `setter`가 호출된다. 객체 생성 시 기본 값 대입은 `setter`가 호출되지 않는다.
- 클래스 내부 필드에 `private`을 선언하고 getter/setter를 정의해주지 않으면 **getter/setter를 통하지 않고 바로 뒷받침하는 필드에 접근하도록 바이트 코드를 최적화 해준다.**
  - `val` 필드를 선언하면서 `getter`에서 `field`를 쓰지 않으면 **컴파일러가 뒷받침하는 필드를 생성하지 않는다.**
  - `var` 필드 일때는 **getter/setter 양쪽에서 모두 `field`를 사용하지 않는 경우에만 뒷받침하는 필드를 생성하지 않는다.**  
- getter/setter가 상태를 저장하기 위해 뒷받침하는 필드를 사용하는 대신, 자바처럼 다른 프로퍼티를 상태 저장에 사용할 수도 있다.
  - 이런 경우 getter/setter가 상태 저장을 위해 사용하는 프로퍼티를 **뒷받침하는 프로퍼티**라 한다.

# **지연 초기화 프로퍼티**

프로퍼티를 `var`로 선언하면서 초기화 값을 바로 알 수 없거나, 가능하면 선언 시점을 최대한 늦추고 싶은 경우 `null`로 초기화한 후 나중에 원하는 값을 대입하는 방법을 사용하게 되면 해당 필드는 null을 허용하는 필드가 되어버린다.  
**lateinit var** 키워드로 프로퍼티를 선언하면 **초기화를 미룰 수 있게 해준다.**
- **최상위나 클래스, 객체 내부에 선언할 수 있으며 참조 타입에 대해서만 사용이 가능하다.**
- 주 생성자 파라미터와 함수나 블록내에 지역적으로는 선언할 수 없다.
- lateinit var 프로퍼티에는 getter/setter를 선언할 수 없다.

# **프로퍼티 게터와 인자가 없는 함수 중 어느 것을 사용해야 할까?**

프로퍼티 getter는 인자가 없는 함수와 큰 차이가 없어 보인다.  
**언제 함수를 사용하고 언제 getter를 사용해야할까?**  
1. 상태를 표현하면 프로퍼티, 동작을 표현하면 함수를 사용한다.
2. 여러 번 호출해도 값이 달라지지 않으면 프로퍼티를 사용하고, 다른 부수 효과나 예외 가능성이 있다면 함수를 사용한다.
3. 항상 같은 결과를 내놓더라도 계산 비용이 많이 드는 경우에는 함수를 사용해 복잡한 계산이 이뤄질 수 있음을 표현한다.
   - 결과를 캐싱할 수 있다면 프로퍼티를 사용해도 좋다.

# **코틀린의 예외**

코틀린에는 `Throwable` 외에 자주 볼 수 있는 몇 가지 예외 클래스가 정의돼 있다.  
JVM에서 돌아가는 코틀린의 경우 실제로는 자바에 정의된 예외 클래스들이다.  

- **Exception**
  - `Throwable`을 상속한 클래스이며, **자바의 모든 체크 예외는 Exception의 하위 클래스여야 한다.**
- **RuntimeException**
  - **자바의 모든 언체크 예외는 RuntimeException의 하위 클래스여야 한다.**
- **Error**
  - `Throwable`을 상속한 클래스로, 심각한 문제를 표현하는 예외를 다룬다.
  - `AssertionError`, `NotImplementedError`, `OutOfMemoryError` ...
  
`catch`를 써서 예외를 잡아내더라도 **프로그램 전체에서 발생한 오류를 한 군데서 잡아내기 보다는 프로그래머가 원하는 영역에서 발생한 오류를 원하는 영역 안에서 잡아낼 수 있으면 좋을 것이다.**  
코틀린은 아쉽게도 `여러 예외를 한꺼번에 나열하는 문법을 제공하진 않는다.`  
하지만 **예외의 타입 계층과 `is`연산을 사용하면 비슷한 결과를 얻을 수 있다.**  

```kotlin
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

describe("상속 관계인 예외 클래스") {

    val parent = CommonException("부모 예외 클래스")
    val child1 = FirstLevelException1("1레벨 자식 예외 클래스")
    val child2 = SecondLevelException("2레벨 자식 예외 클래스")
    val other = OtherException("그 외 예외 클래스")

    context("자식 클래스 is 부모 클래스는 참이다.") {
        (child1 is CommonException) shouldBe true
        (child2 is FirstLevelException1) shouldBe true
        (child2 is CommonException) shouldBe true
//            (other is CommonException) shouldBe false // 컴파일 에러
    }

    context("throwException 함수는") {
        it("난수를 기준으로 예외를 던진다.") {
            try {
                throwException()
            } catch (e: Throwable) {
                when (e) {
                    is SecondLevelException -> println("2레벨 자식 예외 클래스")
                    is FirstLevelException1 -> println("1레벨 자식 예외 클래스")
                    is CommonException -> println("부모 예외 클래스")
                }
            } catch (_: OtherException) {
                println("그 외 예외 클래스")
            }
        }
    }
}
```
  
`if/when`을 식으로 쓰는 것과 같이 `try/catch`도 식으로 쓸 수 있다.  
`try`에서 처리한 결과를 값으로 변수에 담고 싶거나 멤버 함수 호출을 연속적으로 사용하는 중간에 예외가 발생할 수 있는데, 이 예외를 받아서 적절한 값을 지정해주고 싶을 때 사용할 수 있다.  

```kotlin
class StringToInt(
    val str: String
) {
    fun throwException() = try { str.toInt() } catch (e: NumberFormatException ) { throw e }
    fun returnZero() = try { str.toInt() } catch (e: NumberFormatException ) { 0 }
}

describe("StringToInt 클래스는") {
    val str = StringToInt("문자열")
    val number = StringToInt("1234")

    context("한글을 정수로 변환을 시도한다면") {

        it("throwException 함수는 NumberFormatException 을 던진다.") {
            shouldThrow<NumberFormatException> { str.throwException()  }
        }

        it("returnZero 함수는 0을 반환한다.") {
            str.returnZero() shouldBe 0
        }
    }

    context("숫자 문자열을 정수로 변환을 시도한다면") {
        it("정수를 반환한다.") {
            number.throwException() shouldBe 1234
            number.returnZero() shouldBe 1234
        }
    }
}
```

# **Nothing 이라는 특별한 타입을 왜 도입했을까?**

예외가 발생했을 때 적절한 디폴트 값이 없다면 코틀린 타입 검사기가 `catch`절의 타입을 **Nothing** 타입으로 간주해 타입 검사를 통과시킨다.  

```kotlin
val throwNewException = try { str.toInt() } catch (e: NumberFormatException) { throw InputException(e) }
val result = if(bool) 0 else throw InputException()

val exception: Nothing = throw InputException()
```

`try/catch`와 `if/else`문에서 발생할 수 있는 **throw의 타입은 모든 코틀린 타입의 하위 타입이어야 한다.**  
이런 용도를 위해 **Nothing**이라는 타입을 사용한다.  
  
**Nothing** 타입은 **정상적인 프로그램 흐름이 아닌 경우를 표현하는 타입**이기 때문에 `두 가지의 경우를 제외`하고는 인스턴스를 생성할 수 없다.  

```kotlin
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
``` 
  
위의 예제처럼 반환 타입이 `Nothing`이기 위해서는 `return`으로 뭔가 값을 반환하면 타입이 일치하지 않아 `throw`를 선언하거나 아예 값을 반환하지 않는 함수를 작성해야 한다.  
하지만 위의 코드는 실행하자마자 프로그램이 종료되거나 무한 루프를 돌기 때문에 **Nothing 타입의 값을 실행 시점에 얻을 방법은 없다.**
  
모드 타입의 상위 타입이면서 인스턴스도 만들 수 있는 **Any**와는 `인스턴스가 존재할 수 없다는 차이점`이 있다.  
따라서 `Nothing`은 컴파일러가 타입 검사를 할 때만 사용하는 **상징적인 타입**이라고 볼 수 있다.  
