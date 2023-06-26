package playground

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface MyCoroutineContext {
    operator fun <E : Element> get(key: Key<E>): E?
    fun <R> fold(initial: R, operation: (R, Element) -> R): R
    operator fun plus(context: MyCoroutineContext): MyCoroutineContext
    fun minusKey(key: Key<*>): MyCoroutineContext

    interface Key<E : Element>

    interface Element : MyCoroutineContext {
        public val key: Key<*>
    }
}

typealias TheCoroutineContext =
    Map<MyCoroutineContext.Key<MyCoroutineContext.Element>, MyCoroutineContext.Element>

// custom playground.getCoroutine context
data class SomeCoroutine(
    val name: String
) : AbstractCoroutineContextElement(SomeCoroutine) {
    companion object Key : CoroutineContext.Key<SomeCoroutine>

    override fun toString(): String = "CoroutineName($name)"
}

suspend fun name(): String? =
    coroutineContext[SomeCoroutine]?.name
