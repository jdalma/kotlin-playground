package _50_DesginPatterns.structure

fun main() {
    val trooper = StormTrooper(Rifle(), RegularLegs())

    val firstSquad = Squad(listOf(trooper.copy(), trooper.copy(), trooper.copy()))
    val secondSquad = Squad(trooper.copy(), trooper.copy(), trooper.copy())

    val platoon: Squad = Squad(firstSquad, secondSquad)
}

class Squad(private val units: List<Trooper>) : Trooper {

    constructor(vararg units: Trooper) : this(units.toList())

    override fun move(x: Long, y: Long) {
        for (u in units) {
            u.move(x, y)
        }
    }

    override fun attackRebel(x: Long, y: Long) {
        for (u in units) {
            u.attackRebel(x, y)
        }
    }
}
