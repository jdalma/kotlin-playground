
# [연습문제](https://github.com/jdalma/kotlin-core-programming/wiki/%EC%97%B0%EC%8A%B5-%EB%AC%B8%EC%A0%9C)

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

# 예제 작성 필요
1. `===` , `hashCode` , `equals`
2. 스마트 캐스트

# 1회차 모임 `2023.05.10` : 2장. 프로그램을 이루는 기본 단위: 변수와 식, 문, 3장. 함수

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

# 2회차 모임 `2023.05.17` : 4장. 클래스와 객체, 5장. 예외 처리

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
