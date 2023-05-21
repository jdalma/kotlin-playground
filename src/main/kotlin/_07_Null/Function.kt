package _07_Null


fun <T> find(arr: Array<T>, predicate: (T) -> Boolean) : T? {
    for (ele in arr) {
        if (predicate(ele)) {
            return ele
        }
    }
    return null
}
