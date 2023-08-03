package typeclasses

interface Operations {
  fun sum(a: Int, b: Int): Int
  fun multiply(a: Int, b: Int): Int
}

class OperationsBase : Operations {
  override fun sum(a: Int, b: Int): Int = a + b
  override fun multiply(a: Int, b: Int): Int = a * b
}

class OperationsComplex(private val operations: OperationsBase) : Operations {
  override fun sum(a: Int, b: Int): Int = operations.sum(a, b)
  override fun multiply(a: Int, b: Int): Int = operations.multiply(a, b)
  fun divide(a: Int, b: Int): Int = a / b
}

class OperationsDelegation(private val operations: Operations) : Operations by operations {
    fun divide(a: Int, b: Int): Int = a / b
}

fun main() {
    val operationsBase = OperationsBase()
    val delegation = OperationsComplex(operationsBase)
    println(delegation.sum(1, 2))
    println(delegation.multiply(1, 2))
    println(delegation.divide(1, 2))

    // delegation
    val operationsDelegation = OperationsDelegation(operationsBase)
    println(operationsDelegation.sum(1, 2))
    println(operationsDelegation.multiply(1, 2))
    println(operationsDelegation.divide(1, 2))
}
