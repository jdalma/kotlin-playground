package _50_DesginPatterns.behavioral

import kotlin.random.Random

fun main() {
    // 달팽이 예제
    val snail = Snail()
    snail.seeHero()
    snail.getHit(1)
    snail.getHit(10)

    // 피자 배달 상태 예제
    var order: PizzaOrderStatus = OrderReceived(Random.nextInt())
    order = order.nextStatus()
    order = order.nextStatus()
    order = order.nextStatus()
}

interface WhatCanHappen {
    fun seeHero()
    fun getHit(pointsOfDamage: Int)
    fun calmAgain()
}

class Snail : WhatCanHappen {
    private var healthPoints = 10
    private var mood: Mood = Mood.Still

    override fun seeHero() {
        mood = when (mood) {
            is Mood.Still -> {
                println("Aggressive")
                Mood.Aggressive
            }
            else -> {
                println("No change")
                mood
            }
        }
    }

    override fun getHit(pointsOfDamage: Int) {
        println("Hit for $pointsOfDamage points")
        healthPoints -= pointsOfDamage

        println("Health: $healthPoints")
        mood = when {
            (healthPoints <= 0) -> {
                println("Dead")
                Mood.Dead
            }
            mood is Mood.Aggressive -> {
                println("Retreating")
                Mood.Retreating
            }
            else -> {
                println("No change")
                mood
            }
        }
    }

    override fun calmAgain() {
    }
}

sealed class Mood {
    // Some abstract methods here, like draw(), for example
    object Still : Mood()
    object Aggressive : Mood()
    object Retreating : Mood()
    object Dead : Mood()
}

sealed class PizzaOrderStatus(protected val orderId: Int) {
    abstract fun nextStatus(): PizzaOrderStatus
}

class OrderReceived(orderId: Int) : PizzaOrderStatus(orderId) {
    override fun nextStatus() = PizzaBeingMade(orderId)
}

class PizzaBeingMade(orderId: Int) : PizzaOrderStatus(orderId) {
    override fun nextStatus() = OutForDelivery(orderId)
}

class OutForDelivery(orderId: Int) : PizzaOrderStatus(orderId) {
    override fun nextStatus() = Completed(orderId)
}

class Completed(orderId: Int) : PizzaOrderStatus(orderId) {
    override fun nextStatus() = this
}
