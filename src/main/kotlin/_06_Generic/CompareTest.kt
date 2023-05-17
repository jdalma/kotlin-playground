package _06_Generic

class Account<T: Comparable<T>>(initialBalance: T): Comparable<Account<T>> {

    private val balance = initialBalance
    override fun compareTo(other: Account<T>): Int =
            balance.compareTo(other.balance)
}

fun <T> copyWhenGenerator(list: List<T>, threshold: T): List<String>
    where T: CharSequence,
          T: Comparable<T> {
              return list.filter {
                  it > threshold
              }.map { it.toString() }
          }
