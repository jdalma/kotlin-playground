
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
