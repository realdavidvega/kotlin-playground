package com.functional

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.ResolvableType

fun SpringApplication.addInitializers(fa: () -> List<ApplicationContextInitializer<*>>): Unit =
  setInitializers(initializers.apply { addAll(fa.invoke()) })

inline fun <reified T : Any> springApplication(
  init: SpringApplication.() -> Unit
): SpringApplication = SpringApplication(T::class.java).apply(init)

@Suppress("SpreadOperator")
fun <A> ConfigurableApplicationContext.getBeanWithGenerics(
  aParent: Class<A>,
  vararg generics: Class<*>
): A {
  val genericTypes = generics.map { ResolvableType.forClass(it) }.toTypedArray()
  val resolvableType = ResolvableType.forClassWithGenerics(aParent, *genericTypes)
  val beanNames = getBeanNamesForType(resolvableType)
  require(beanNames.size == 1) { "Expected exactly one bean for type $resolvableType" }
  val beanName = beanNames[0]
  return getBean(beanName, aParent)
}
