package _50_DesginPatterns.behavioral

fun main() {
    val catTheConductor = Cat()

    val bat = Bat()
    val dog = Dog()
    val turkey = Turkey()

    catTheConductor.joinChoir(dog::howl)
    catTheConductor.joinChoir(dog::bark)
    catTheConductor.joinChoir(turkey::gobble)

    catTheConductor.conduct()
    catTheConductor.conduct()
}

class Turkey {
    fun gobble() {
        println("Gob-gob")
    }
}

class Dog {
    fun bark() {
        println("Woof")
    }

    fun howl() {
        println("Auuuu")
    }
}

class Cat {
    private val participants = mutableMapOf<() -> Unit, () -> Unit>()

    fun joinChoir(whatToCall: () -> Unit) {
        participants[whatToCall] = whatToCall
    }

    fun leaveChoir(whatNotToCall: () -> Unit) {
        participants.remove(whatNotToCall)
    }

    fun conduct() {
        for (p in participants.values) {
            p()
        }
    }
}

typealias Times = Int

enum class SoundPitch { HIGH, LOW }
interface Message {
    val repeat: Times
    val pitch: SoundPitch
}


data class LowMessage(override val repeat: Times) : Message {
    override val pitch = SoundPitch.LOW
}

data class HighMessage(override val repeat: Times) : Message {
    override val pitch = SoundPitch.HIGH
}

class Bat {
    fun screech(message: Message) {
        when (message) {
            is HighMessage -> {
                for (i in 1 .. message.repeat) {
                    println("${message.pitch} 이-----")
                }
            }
            else -> println("낼 수 없는 소리 ㅠ")
        }
    }
}
