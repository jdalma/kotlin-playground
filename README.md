
# [연습문제](연습문제.md)

# 무지 목록

1. `reified`
2. `vtable`
3. `suspend`, `yield`
4. 코루틴 컨텍스트를 지정하기 위해 사용되는 `@Context`
   - [Context Receivers](https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md)
   - [Coroutines](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md)
5. **람다 팩토리 구조**
6. `invoke`와 `invoke dynamic` 차이점
7. 코틀린 인 액션에 `lateinit`이 만들어진 이유가 잘 나와있다고 한다.
   - `lateinit var` 프로퍼티에 대해서는 게터와 세터를 정의할 수 없다.
8. [Delegated properties](https://kotlinlang.org/docs/delegated-properties.html)
9. 오버로드 해소 규칙

# 예제 작성 필요



# 1회차 모임 `2023.05.10`

## 2장. 프로그램을 이루는 기본 단위: 변수와 식, 문, 3장. 함수

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

# 2회차 모임 `2023.05.17`

## 4장. 클래스와 객체, 5장. 예외 처리

1. `data class`는 객체지향을 파괴하고 값 지향으로 개발하게 만든다.
   - 함수형의 핵심은 값을 지향하는 것
   - 코틀린으로 객체지향으로 사용하려면 의식적인 노력이 필요하다.
2. **클래스는 객체를 생성하기 위한 틀 비슷한 역할을 하는 동시에 새로운 타입을 정의한다.**
3. `===` 언제써야할까?
   - 맹대표님은 쓸 일이 없다고 생각
   - [Structural equality](https://kotlinlang.org/docs/equality.html#structural-equality)
6. 클래스 `init` 블록을 사용하기 보다는 `property delegate`? 또는 `lazy`하게 설정하던 한다
   - [Delegated properties](https://kotlinlang.org/docs/delegated-properties.html)
   - 생성자 블록에 **많은 가공 로직**이 포함되고 **사이드 이펙트** 문제가 존재한다.
   - ```kotlin
     class A(재료){
       val a by(a관련재료)
       val b by(b관련재료)
       init{ 사이드이팩트 }
     }
     ```
   - `init` 블록에 사이드 이펙트롤 가둬놓기 
8. `hashCode()`를 수정하면 `equals()`에 영향을 준다.
9. 스마트 캐스트는 `val`에만 적용한다.
10. 인터페이스 다중 구현할 때 시그니처가 똑같은 함수를 `super`를 통해 지정하여 다이아몬드 문제를 해결할 수 있는데, 왜 다중 상속은 안풀어주는걸까?
    - 자바 호환성을 위해 그렇지 않을까 라고 생각하신다
11. 맹대표님은 내부 함수들이 발생시키는 예외를 외부 (호출자 입장에서) `try/catch/catch ...` 와 같은 처리는 문제가 된다고 생각

# 3회차 모임 `2023.05.23` 7장. 널 가능성

## 6장. 제네릭

1. 널러블 세탁이나 가시성을 위해 백킹필드를 사용할 때 `_`를 붙여서 사용한다.
   - [`kotlinlang` properties](https://kotlinlang.org/docs/properties.html#backing-properties)
   - ```kotlin
     private var _map:HashMap<String,String>? = null
     private val map get() = _map ?: hashMapOf().also{_map = it}
     ```
2. `T`, `I`, `U`와 같은 약자로 타입 파라미터를 지정하기 보다는 명확한 단어를 쓰는것이 좋지 않을까?
   - [`java docs` Generic Types](https://docs.oracle.com/javase/tutorial/java/generics/types.html)
3. **코틀린의 `선언 지점 번셩`과 `사용 지점 변성`에서 사용하는 `in`, `out`의 의미가 다르다** 📌
4. 코틀린에서 `Lower bound`는 왜 제거 되었을까? 📌
5. 함수를 콜 할떄의 상황으로만 분류할 수 있기 때문에 함수의 시그니처에 반환 타입은 포함되지 않는다.
   - 내부에서 제네릭을 사용하는 입장에서는 단순한 `T` 타입을 사용하는 것이 아니라 해당 `T` 타입의 대체 가능성을 사용하는 것이다.
6. 람다에서는 어떻게 변성을 확인할까? **이펙티브 자바에 나오는 PECS의 특성이 이미 함수형 인터페이스에 적용되어 있다.**

- ```kotlin
   val numToAny: (Number) -> Any = { v:Number -> "Hahaha" }
   val numToBoolean: (Number) -> Boolean = { v:Number -> true }
   val intToAny: (Int) -> Any = { i:Int -> "$i" }
   val intToBoolean: (Int) -> Boolean = { i:Int -> true }
   
   val numToAnyFun1: (Number) -> Any = numToAny
   val numToAnyFun2: (Number) -> Any = numToBoolean
   //        val numToAnyFun3: (Number) -> Any = intToAny      // 컴파일 에러
   //        val numToAnyFun4: (Number) -> Any = intToBoolean  // 컴파일 에러
   
   //        val numToBooleanFun1: (Number) -> Boolean = numToAny      // 컴파일 에러
   val numToBooleanFun2: (Number) -> Boolean = numToBoolean
   //        val numToBooleanFun3: (Number) -> Boolean = intToAny      // 컴파일 에러
   //        val numToBooleanFun4: (Number) -> Boolean = intToBoolean  // 컴파일 에러
   
   val intToAnyFunc1: (Int) -> Any = numToAny
   val intToAnyFunc2: (Int) -> Any = numToBoolean
   val intToAnyFunc3: (Int) -> Any = intToAny
   val intToAnyFunc4: (Int) -> Any = intToBoolean
   ```
7. 타입 간에 상하위 관계가 성립한다는 말은? **리스코프 치환 원칙이 성립 한다는 말이다.**
8. 코틀린은 네이티브 함수 타입? `operator invoke()`등 `Callable` 이라는 특별한 카테고리에 묶인다. 📌

## 7장. 널 가능성

1. [Kotlin Symbol Processing](https://csy7792.tistory.com/355) 📌
2. [제네릭 유형 속성이 null을 허용하는 이유는 무엇입니까?](https://stackoverflow.com/questions/33021802/why-is-a-generic-typed-property-nullable)
