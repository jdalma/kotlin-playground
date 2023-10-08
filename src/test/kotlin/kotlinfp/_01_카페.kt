@file:Suppress("UNCHECKED_CAST")
package kotlinfp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import java.lang.RuntimeException
import kotlin.reflect.KClass

interface CreditCard
interface Product {
    val price: Double
    val factory: () -> Product
}

/**
 * KClass<PRODUCT> 로 클래스 타입을 받아서 해당 타입에 맞게 람다 팩토리로 만들어 낸다.
 */
class Charge<PRODUCT: Product> (
    val cc: CreditCard,
    val qty: Int,
    val product: KClass<PRODUCT>
)

class Receipt<PRODUCT: Product> (
    val qty: Int,
    val product: KClass<PRODUCT>
)

class Americano: Product {
    override val price: Double get() = 1000.0

    // 람다의 반환 타입은 상위 Product 이지만 구현체에서 하위 반환 타입을 선언해도 위배되지 않는다.
    // 코틀린의 함수에서는 반환 값에 대한 공변성이 성립한다.
    override val factory: () -> Americano get() = ::Americano
}

class CafeLatte: Product {
    override val price: Double get() = 2000.0
    override val factory: () -> CafeLatte get() = ::CafeLatte
}

class Cafe {
    @PublishedApi internal val factories: HashMap<KClass<*>, () -> Product> = hashMapOf()
    fun addFactory(vararg products: Product) {
        products.forEach {
            factories[it::class] = it.factory
        }
    }

    fun <PRODUCT: Product> getProduct(product: KClass<PRODUCT>) : PRODUCT =
        factories[product]!!() as PRODUCT

    // buy, payment, receive 이 세 단계에 대한 구분은 반환 타입으로 구분한다.
    // buy 를 거쳤다면 Charge 타입이어야 하고, payment 를 거쳤다면 Receipt 타입이어야 한다.
    fun <PRODUCT: Product> buy(cc: CreditCard, qty: Int, product: KClass<PRODUCT>): Charge<PRODUCT>? =
        if (qty == 0) null else Charge(cc, qty, product)
    fun <PRODUCT: Product> payment(vararg charges: Charge<PRODUCT>) : Receipt<PRODUCT>? {
        if (charges.isEmpty()) return null

        val totalQty = charges.sumOf { it.qty }
        // 결제 부수효과 진행 (이 부수효과는 payment에만 머물게 한다.)
        val isPaidOk = true
        return if (isPaidOk) Receipt(totalQty, charges.first().product) else null
    }

    // inline 내부 요소에 private 요소가 있으면 해당 위치에 붙여넣을 수 없다.
    // 결론은 모두 public 이어야 한다.
    // 코틀린에서 허용하는 가시성은 인라인으로 까지는 허용하고 싶은데 그래도 객체에서는 숨기고 싶을 때 @PublishedApi internal 를 사용한다.
    // 최대 가시성을 지정할 수 있다.
    inline fun <reified PRODUCT: Product> receive(receipt: Receipt<PRODUCT>) : Array<PRODUCT>? {
        factories[receipt.product] ?: return null
        return Array(receipt.qty) { getProduct(receipt.product) }
    }
}

class _01_카페: StringSpec({

    "카페 테스트" {
        val cafe: Cafe = Cafe().also {
            it.addFactory(Americano(), CafeLatte())
        }
        val myCard = object :CreditCard {}
        val americano = cafe.buy(myCard, 2, Americano::class)?.let { charge ->
            cafe.payment(charge)?.let {
                    receipt -> cafe.receive(receipt)
            }
        }

        americano?.size?.shouldBeEqual(2)
        americano?.first()?.price?.shouldBeEqual(1000.0)
    }
})
