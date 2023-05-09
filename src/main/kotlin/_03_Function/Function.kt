package _03_Function

class Function {

    companion object {

        fun add(x: Int, y: Int): Int {
            return x + y
        }

        /**
         * 단일 식 함수는 계산한 결과가 함수의 결괏값이므로, 식 내부에서 return 문을 쓰지 못한다.
         */
        fun add2(x: Int, y: Int) = x + y

        /**
         * 익명 함수
         * 꼭 단일식 함수 형태로만 익명 함수를 정의할 수 있는 것은 아니다.
         */
        val add3 = fun (x: Int, y: Int) = x + y
        val add4 = fun (x: Int, y: Int): Int { return x + y }

        /**
         * 람다
         */
        val add5 = { x: Int, y: Int -> x + y }

        fun returnUnit() {
            // do something...
        }

        /**
         * 변수 스코프 확인해보기
         */
        val x = 1
        fun nameShadowingTest() {
            bar(x)
            foo(x+10)
        }
        fun foo(y: Int) {
            fun bar(x: Int) {
                println(y) // 11
                println(x) // 10
            }
            println(x) // 1
            bar(10)
        }
        fun bar(x: Int) {
            var x = x
            x = x - 1
            println(x) // 0
        }

        /**
         * 일급 시민과 고차 함수 예제
         */
        fun applyAfterCompose(
            value: Int,
            f: (Int) -> Int,
            g: (Int) -> Int) = g(f(value)
        )

        val fourAddTwoAndThenSquared = applyAfterCompose(
            4,
            { x: Int -> x + 2 },
            { x: Int -> x * x }
        )

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

        /**
         * 람다를 익명 함수로 변경해보자
         */
        val example1 : (Int) -> Double = { it * 2.0 }
        // 식 본문 익명 함수
        val example1_1 : (Int) -> Double = fun(x: Int) = x * 2.0
        // 블록 본문 익명 함수
        val example1_2 : (Int) -> Double = fun(x): Double { return x * 2.0}

        val example2 : () -> Double = { 2.0 }
        // 식 본문 익명 함수
        val example2_1 : () -> Double = fun() = 2.0
        // 블록 본문 익명 함수
        val example2_2 : () -> Double = fun() : Double { return 2.0 }

        val example3 : (Int, String) -> String = { i, s -> "$i : $s" }
        // 식 본문 익명 함수
        val example3_1 : (Int, String) -> String = fun(i , s) = "$i : $s"
        // 블록 본문 익명 함수
        val example3_2 : (Int, String) -> String = fun(i , s) : String { return "$i : $s" }

    }
}
