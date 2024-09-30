package patterns

/**
 * Visitor Pattern
 *
 * The Visitor Pattern separates algorithms from the object structure. Think of a Visitor as someone
 * who visits multiple places and performs different actions depending on the place. This means we
 * modify the Visitor instead of the place itself. It's mostly used to implement new functionalities
 * that don't fit inside an object but are necessary for the new feature to work.
 *
 * What benefits do we get?
 * - Open/Closed Principle: We can add new algorithms without changing the object structure.
 * - Single Responsibility Principle: Each class is responsible for a different behavior.
 * - Extension of a class with minimal to no modification.
 * - Avoidance of polluting the object class. The major downside is that all Visitors must be
 *   updated when a class is added or removed from the hierarchy.
 */
object Visitor {

  // With the usage of sealed class, we can model our domain to specific animals and visitors.

  // Element interface representing animals in the zoo
  sealed interface Animal {
    // Each animal accepts a visitor, which is a zookeeper
    fun accept(visitor: Zookeeper)
  }

  // Visitor interface representing zookeeper
  // Each zookeeper can visit different types of animals
  sealed interface Zookeeper {
    fun visitLion(lion: Lion)

    fun visitElephant(elephant: Elephant)
  }

  // Concrete elements representing different types of animals
  data object Lion : Animal {
    override fun accept(visitor: Zookeeper) {
      visitor.visitLion(this)
    }
  }

  data object Elephant : Animal {
    override fun accept(visitor: Zookeeper) {
      visitor.visitElephant(this)
    }
  }

  // Concrete visitor implementing actions of the zookeeper
  data object AnimalFeeder : Zookeeper {
    override fun visitLion(lion: Lion) {
      println("Feeding lion...")
    }

    override fun visitElephant(elephant: Elephant) {
      println("Feeding elephant...")
    }
  }

  // Concrete visitor implementing actions of the zookeeper
  data object AnimalDoctor : Zookeeper {
    override fun visitLion(lion: Lion) {
      println("Checking health of lion...")
    }

    override fun visitElephant(elephant: Elephant) {
      println("Checking health of elephant...")
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val lion = Lion
    val elephant = Elephant

    val feeder = AnimalFeeder
    val doctor = AnimalDoctor

    lion.accept(feeder) // Feeding lion...
    elephant.accept(doctor) // Checking health of elephant...
  }
}
