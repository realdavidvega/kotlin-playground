@file:Suppress("Unused")

package language

// Inheritance and delegation

object Delegation {
  interface Operations {
    fun sum(a: Double, b: Double): Double

    fun multiply(a: Double, b: Double): Double
  }

  open class OperationsOpen : Operations {
    override fun sum(a: Double, b: Double): Double = a + b

    override fun multiply(a: Double, b: Double): Double = a * b
  }

  class OperationsExtends : OperationsOpen() {
    fun divide(a: Double, b: Double): Double = a / b
  }

  class OperationsBase : Operations {
    override fun sum(a: Double, b: Double): Double = a + b

    override fun multiply(a: Double, b: Double): Double = a * b
  }

  class OperationsParam(private val operations: OperationsBase) : Operations {
    override fun sum(a: Double, b: Double): Double = operations.sum(a, b)

    override fun multiply(a: Double, b: Double): Double = operations.multiply(a, b)

    fun divide(a: Double, b: Double): Double = a / b
  }

  class OperationsDelegation(private val operations: Operations) : Operations by operations {
    fun divide(a: Double, b: Double): Double = a / b
  }

  fun main() {
    println("Extends:")
    val extends = OperationsExtends()
    println(extends.sum(1.0, 2.0))
    println(extends.multiply(1.0, 2.0))
    println(extends.divide(1.0, 2.0))
    println("")

    // base impl
    val base = OperationsBase()

    println("Param:")
    val param = OperationsParam(base)
    println(param.sum(1.0, 2.0))
    println(param.multiply(1.0, 2.0))
    println(param.divide(1.0, 2.0))
    println("")

    println("Delegation:")
    val delegation = OperationsDelegation(base)
    println(delegation.sum(1.0, 2.0))
    println(delegation.multiply(1.0, 2.0))
    println(delegation.divide(1.0, 2.0))
    println("")
  }
}
