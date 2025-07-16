@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package extensions.arrow

import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.identity
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.fold
import arrow.core.raise.merge
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

typealias Null = Nothing?

typealias Nes<A> = NonEmptySet<A>

/**
 * Old type extensions for [arrow.core.raise.Raise]
 *
 * As some of them were removed from the library in later versions after 2.0.0.
 *
 * Modern alternative involve the usage of [arrow.core.raise.SingletonRaise].
 */

// Equivalent to [arrow.core.raise.SingletonRaise] with Nothing?
class NullableRaise(private val raise: Raise<Null>) : Raise<Null> by raise {
  fun ensure(value: Boolean): Unit = raise.ensure(value) { null }

  fun <A> Option<A>.bind(): A = getOrElse { raise(null) }

  fun <A> A?.bind(): A {
    contract { returns() implies (this@bind != null) }
    return this ?: raise(null)
  }

  @JvmName("bindAllNullable")
  fun <K, V> Map<K, V?>.bindAll(): Map<K, V> = mapValues { (_, v) -> v.bind() }

  @JvmName("bindAllNullable") fun <A> Iterable<A?>.bindAll(): List<A> = map { it.bind() }

  fun <A> ensureNotNull(value: A?): A {
    contract { returns() implies (value != null) }
    return raise.ensureNotNull(value) { null }
  }

  inline fun <A> recover(@BuilderInference block: NullableRaise.() -> A, recover: () -> A): A =
    when (val nullable = nullable(block)) {
      null -> recover()
      else -> nullable
    }
}

// Equivalent to [arrow.core.raise.Raise] with Throwable
class ResultRaise(private val raise: Raise<Throwable>) : Raise<Throwable> by raise {
  fun <A> Result<A>.bind(): A = fold(::identity) { raise(it) }

  @JvmName("bindAllResult")
  fun <K, V> Map<K, Result<V>>.bindAll(): Map<K, V> = mapValues { (_, v) -> v.bind() }

  @JvmName("bindAllResult") fun <A> Iterable<Result<A>>.bindAll(): List<A> = map { it.bind() }

  @JvmName("bindAllResult") fun <A> Nel<Result<A>>.bindAll(): Nel<A> = map { it.bind() }

  @JvmName("bindAllResult")
  fun <A> Nes<Result<A>>.bindAll(): Nes<A> = map { it.bind() }.toNonEmptySet()

  inline fun <A> recover(
    @BuilderInference block: ResultRaise.() -> A,
    recover: (Throwable) -> A,
  ): A = result(block).fold(onSuccess = { it }, onFailure = { recover(it) })
}

// Equivalent to [arrow.core.raise.SingletonRaise] with None
class OptionRaise(private val raise: Raise<None>) : Raise<None> by raise {
  fun <A> Option<A>.bind(): A = getOrElse { raise(None) }

  @JvmName("bindAllOption")
  fun <K, V> Map<K, Option<V>>.bindAll(): Map<K, V> = mapValues { (_, v) -> v.bind() }

  @JvmName("bindAllOption") fun <A> Iterable<Option<A>>.bindAll(): List<A> = map { it.bind() }

  @JvmName("bindAllOption")
  fun <A> NonEmptyList<Option<A>>.bindAll(): NonEmptyList<A> = map { it.bind() }

  @JvmName("bindAllOption")
  fun <A> NonEmptySet<Option<A>>.bindAll(): NonEmptySet<A> = map { it.bind() }.toNonEmptySet()

  fun ensure(value: Boolean): Unit = ensure(value) { None }

  fun <A> ensureNotNull(value: A?): A {
    contract { returns() implies (value != null) }
    return ensureNotNull(value) { None }
  }

  inline fun <A> recover(@BuilderInference block: OptionRaise.() -> A, recover: () -> A): A =
    when (val option = option(block)) {
      is None -> recover()
      is Some<A> -> option.value
    }
}

inline fun <A> nullable(block: NullableRaise.() -> A): A? = merge {
  block(NullableRaise(raise = this))
}

inline fun <A> result(block: ResultRaise.() -> A): Result<A> =
  fold(
    block = { block(ResultRaise(this)) },
    catch = Result.Companion::failure,
    recover = Result.Companion::failure,
    transform = Result.Companion::success,
  )

inline fun <A> option(block: OptionRaise.() -> A): Option<A> =
  fold(block = { block(OptionRaise(this)) }, recover = ::identity, transform = ::Some)
