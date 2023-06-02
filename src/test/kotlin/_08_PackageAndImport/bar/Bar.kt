package _08_PackageAndImport.bar

// import _08_PackageAndImport.foo.bar  // 임포트 한 foo의 bar()가 우선이다.

// import _08_PackageAndImport.foo.* // 가까이 있는 bar()가 우선이다.
// 와일드카드로 임포트하면 foo안의 모든 클래스를 가져오는 것이 아니라 실제로 사용 되는것만 임포트 된다.
// 변수 쉐도잉이 일어나듯이 와일드카드로 임포트하는 것은 우선순위에 밀리게 된다.

fun bar() {
    println("bar.bar()")
}

fun main() {
    bar()
}
