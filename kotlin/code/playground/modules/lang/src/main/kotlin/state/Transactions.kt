@file:Suppress("MagicNumber")

package state

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.stm.STM
import arrow.fx.stm.TMap
import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import arrow.fx.stm.check
import arrow.fx.stm.stm
import java.math.BigDecimal
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Transactions and Software Transactional Memory (STM)
 *
 * Software transactional memory using `arrow-fx-stm`.
 */
object Transactions {

  @JvmInline value class PersonId(val value: Long)

  @JvmInline value class PersonName(val value: String)

  data class Person(val id: PersonId, val name: PersonName)

  @JvmInline value class AccountId(val value: Long)

  @JvmInline value class Balance(val value: BigDecimal)

  data class Account(val id: AccountId, val balance: Balance, val owner: Person)

  sealed interface Error {
    data object NotEnoughBalance : Error {
      override fun toString(): String = "Not enough balance in the account!"
    }

    data class AccountNotFound(val accountId: AccountId) : Error {
      override fun toString(): String = "Account with id '${accountId.value}' was not found"
    }

    data class Generic(val throwable: Throwable) : Error {
      override fun toString(): String = "An unexpected error happened: ${throwable.message}"
    }
  }

  interface Accounts {
    // STM context provides transactional operations
    context(STM, Raise<Error>)
    fun transfer(from: TVar<Account>, to: TVar<Account>, amount: BigDecimal)

    companion object {
      operator fun invoke(service: AccountsService): Accounts =
        object : Accounts {
          context(STM, Raise<Error>)
          override fun transfer(from: TVar<Account>, to: TVar<Account>, amount: BigDecimal) {
            service.withdraw(from, amount)
            service.deposit(to, amount)
          }
        }
    }
  }

  interface AccountsService {
    context(STM)
    fun deposit(acc: TVar<Account>, amount: BigDecimal)

    context(STM, Raise<Error.NotEnoughBalance>)
    fun withdraw(acc: TVar<Account>, amount: BigDecimal)

    context(STM)
    fun withdrawOrRetry(acc: TVar<Account>, amount: BigDecimal)

    context(STM)
    fun withdrawOrElse(acc: TVar<Account>, amount: BigDecimal, fallback: STM.() -> Unit)

    companion object {
      operator fun invoke(): AccountsService =
        object : AccountsService {
          // a function using STM as a receiver does not perform any computations
          // we say it's just a description of a transaction
          context(STM)
          override fun deposit(acc: TVar<Account>, amount: BigDecimal) {
            acc.modify { account ->
              account.copy(balance = Balance(account.balance.value + amount))
            }
          }

          context(STM, Raise<Error.NotEnoughBalance>)
          override fun withdraw(acc: TVar<Account>, amount: BigDecimal) {
            val account = acc.read()
            ensure(account.balance.value - amount >= BigDecimal.ZERO) { Error.NotEnoughBalance }
            acc.write(account.copy(balance = Balance(account.balance.value - amount)))
          }

          context(STM)
          override fun withdrawOrRetry(acc: TVar<Account>, amount: BigDecimal) {
            val account = acc.read()
            // this would retry and thus wait until enough money is in the account
            // 'retry()' could also be used here
            check(account.balance.value - amount >= BigDecimal.ZERO)
            acc.write(account.copy(balance = Balance(account.balance.value - amount)))
          }

          context(STM)
          override fun withdrawOrElse(
            acc: TVar<Account>,
            amount: BigDecimal,
            fallback: STM.() -> Unit,
          ): Unit =
            // orElse allows detecting if a branch has called retry and then use a fallback instead
            stm {
              val account = acc.read()
              check(account.balance.value - amount >= BigDecimal.ZERO)
              acc.write(account.copy(balance = Balance(account.balance.value - amount)))
            } orElse (fallback)
        }
    }
  }

  interface ServiceRepository {
    suspend fun save(account: Account): AccountId

    context(Raise<Error.AccountNotFound>)
    suspend fun find(accountId: AccountId): Account

    suspend fun updateBalance(accountId: AccountId, balance: Balance)

    companion object {
      operator fun invoke(accounts: TMap<AccountId, Account>): ServiceRepository =
        object : ServiceRepository {
          override suspend fun save(account: Account): AccountId =
            // inserting an element, we can also delete with 'remove'
            atomically { accounts.insert(account.id, account) }.let { account.id }

          context(Raise<Error.AccountNotFound>)
          override suspend fun find(accountId: AccountId): Account =
            // reading an element
            atomically { accounts.lookup(accountId) } ?: raise(Error.AccountNotFound(accountId))

          override suspend fun updateBalance(accountId: AccountId, balance: Balance) {
            // update if exists, if not, do nothing
            // we could use 'member' to check if the element exists first
            atomically { accounts.update(accountId) { account -> account.copy(balance = balance) } }
          }
        }
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val account1 =
        Account(AccountId(1), Balance(BigDecimal(500)), Person(PersonId(1), PersonName("John")))

      // will create new TVar with the value of account 1
      val acc1: TVar<Account> = TVar.new(account1)

      val account2 =
        Account(AccountId(2), Balance(BigDecimal(300)), Person(PersonId(2), PersonName("Jane")))
      val acc2: TVar<Account> = TVar.new(account2)

      // check initial balances
      println("account 1 balance is: ${acc1.unsafeRead().balance.value}") // should be 500
      println("account 2 balance is: ${acc2.unsafeRead().balance.value}") // should be 300

      val service = AccountsService()
      val accounts = Accounts(service)

      // running a transaction is done when using 'atomically'
      either { atomically { accounts.transfer(acc1, acc2, BigDecimal(50)) } }
        .getOrElse { error -> println(error) }

      // check final balances
      println("account 1 new balance is: ${acc1.unsafeRead().balance.value}") // should be 450
      println("account 2 new balance is: ${acc2.unsafeRead().balance.value}") // should be 350

      println("------------------------------")

      // error handling
      either { atomically { accounts.transfer(acc1, acc2, BigDecimal(500)) } }
        .getOrElse { error -> println(error) }

      // should keep the same balances
      println("account 1 balance is: ${acc1.unsafeRead().balance.value}") // should be 450
      println("account 2 balance is: ${acc2.unsafeRead().balance.value}") // should be 350

      // not affected
      println("original account 1 variable balance is: ${account1.balance.value}")
      println("original account 2 variable balance is: ${account2.balance.value}")

      println("------------------------------")

      // we can create a TSet or TMap data structures
      // there are other structures like: TQueue, TMVar, TArray, TSemaphore...
      val db: TMap<AccountId, Account> = TMap.new()

      val repository = ServiceRepository(db)

      repository.save(account1).also { println("saved account with id '${it.value}'") }
      repository.save(account2).also { println("saved account with id '${it.value}'") }

      either {
          repository.find(AccountId(2)).also {
            println("found account with id '${it.id.value} and balance: ${it.balance.value}")
          }
        }
        .getOrElse { error -> println(error) }

      println("updating balance...")
      repository.updateBalance(account1.id, Balance(BigDecimal(1000)))

      either {
          repository.find(AccountId(1)).also {
            println("found account with id '${it.id.value} and balance: ${it.balance.value}")
          }
        }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // rollback transactions with catch
      either {
          atomically {
            catch({
              // insert an account
              db.insert(
                AccountId(3),
                Account(
                  AccountId(3),
                  Balance(BigDecimal(1000)),
                  Person(PersonId(3), PersonName("John")),
                ),
              )
              // throw an exception, the transaction will be rolled back
              throw RuntimeException("BOOM!")
            }) { error ->
              raise(Error.Generic(error))
            }
          }
        }
        .getOrElse { error -> println(error) }

      // account not found
      either {
          repository.find(AccountId(3)).also {
            println("found account with id '${it.id.value} and balance: ${it.balance.value}")
          }
        }
        .getOrElse { error -> println(error) }

      println("------------------------------")

      // retry
      // simulate some time until the money is found
      // it is recommended to keep transactions small and never to use code that has side effects
      coroutineScope {
        println("account 1 balance is: ${acc1.unsafeRead().balance.value}") // should be 450
        launch {
          delay(2000)
          atomically { service.deposit(acc1, BigDecimal(1000)) }
        }
        atomically { service.withdrawOrRetry(acc1, BigDecimal(1450)) }
        println(
          "account 1 balance after withdraw is: ${acc1.unsafeRead().balance.value}"
        ) // should be 0
      }

      println("------------------------------")

      // branching
      coroutineScope {
        println("account 1 balance is: ${acc1.unsafeRead().balance.value}") // should be 0
        launch {
          delay(2000)
          atomically { service.deposit(acc1, BigDecimal(1000)) }
        }
        atomically {
          service.withdrawOrElse(acc1, BigDecimal(2000)) {
            println("Ooops! not enough balance in the account!")
          }
        }
      }
    }
  }
}
