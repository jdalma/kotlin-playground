
> **코틀린 핵심 프로그래밍과 코틀린 인 액션에서 나오는 예제들을 `kotest`를 활용하여 학습 테스트를 작성합니다.**  
> - [코틀린 핵심 프로그래밍 - 연습문제](코핵프_연습문제.md)

# 무지 목록

1. `reified`
2. `vtable`
3. 즉시 실행 `suspend`, `yield`
4. 코루틴 컨텍스트를 지정하기 위해 사용되는 `@Context`
   - [Context Receivers](https://github.com/Kotlin/KEEP/blob/master/proposals/context-receivers.md)
   - [Coroutines](https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md)
5. **람다 팩토리 구조**
6. `invoke`와 `invoke dynamic` 차이점
7. 코틀린 인 액션에 `lateinit`이 만들어진 이유가 잘 나와있다고 한다.
   - `lateinit var` 프로퍼티에 대해서는 게터와 세터를 정의할 수 없다.
8. [Delegated properties](https://kotlinlang.org/docs/delegated-properties.html)
9. 오버로드 해소 규칙
10. 코틀린의 internal은 모듈 internal이다.
    11. 패키지 internal이 아니다.

# 예제 작성 필요

1. operator 구현
2. by 키워드에 의한 위임 테스트

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

# 3회차 모임 `2023.05.23`

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

# 4회차 모임 `2023.05.31`

## ~~8장. 패키지와 임포트~~

## 9장. 컬렉션

- 타입 시스템을 통해 기능을 사용하면 하위 호환성에 문제가 생길 가능성이 있다.
  - 예를 들면 자바에서는 `Iterator` 인터페이스를 확장해야 `for`문을 쓸 수 있다.
  - 코틀린은 타입에 의존하지 않고 **연산자 (`operator`)**에 의존한다.
  - 타입을 확장하지 말고 `operator`를 구현 해놓는다면 특정 기능들을 사용할 수 있다.
  - 이러함의 장점은 언어 업데이트가 쉽다는 것이다.
  - **근데 코틀린의 컬렉션 타입 계층을 보면 (`iterator`를 반환하는 `operator` 메소드를 가진) `Iterable`이 존재한다.**
  - 수많은 컬렉션의 함수들은 `Iterable` 계층에서 발생한다.
  - **타입을 가져다쓸까 `operator`를 구현할까 결정해야 할 것이다.**
    - 타입에 대한 유틸 함수를 더 누리고 싶은지와 내가 원하는 용도로만 사용할 것인지
- ```kotlin
  class L{
   operator fun iterator() = this
   operator fun hasNext()
   operator fun next()
  }
  for(i in L)
  ```
  - `iterator`가 `Iterable`을 대신할 수 있다.
  - 그 이유는 **스스로를 반환하는 확장 함수가 존재하기 떄문이다.**
  - `operator fun <T> Iterator<T>.iterator(): Iterator<T>` [참고](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterator/)
- 무의식적으로 `Map`을 사용하면 내부 구현체는 `Linked...`이다. 느리다.
  - 그냥 `HashMap`을 쓰자 

# 5회차 모임 `2023.06.07`

> 코틀린으로 컬렉션을 사용할 때는 최대한 구상 타입을 쓰도록 노력해라.  
> 유틸 함수들이 확장 함수로 구현되어 있기 때문에 부모 타입으로 캐스팅해서 쓰지마라

## 10장. 변수 선언과 기본 타입 자세히 살펴보기

- `Int`만 범위를 벗어나면 예외가 발생한다.
- 부동 소수점의 핵심은 **정규화**다.
  - 정규화를 하면 **1비트를 아낄 수 있다.**
- 맹대표님 회사에서는 실수를 디비에 저장해야 한다면 모두 문자열로 저장한다고 한다.
  - Float과 Int는 쓰지마라, 우리는 다 64비트 프로세서를 쓰고 있기 때문에 Long과 Double을 써라
- "유니코드랑 UTF-8 인디코딩을 손으로 할 수 있을 정도까지 배워놓아라"
- `Range`를 직접 생성해서 사용하면 컴파일러가 최적화를 하지 못한다.

```kotlin
// 아래처럼 작성해야 컴파일되면 일반적인 for문으로 변경된다.
for(i in 1..5)
for(j in 1..5)

// 이렇게 작성하면 Range 객체가 생성되어 해당 함수 호출로 변경된다.
val range = 1..5
for(i in range)
for(j in range)
```
