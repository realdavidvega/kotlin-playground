import arrow.continuations.SuspendApp
import io.github.oshai.kotlinlogging.KotlinLogging
import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.CashAccount
import org.roboquant.brokers.sim.PercentageFeeModel
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.sim.SpreadPricingEngine
import org.roboquant.common.Currency
import org.roboquant.common.USD
import org.roboquant.common.Wallet
import org.roboquant.common.bips
import org.roboquant.loggers.InfoLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PNLMetric
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.ParallelStrategy
import org.roboquant.ta.RSIStrategy

object SimBrokerTest {

  private val logger = KotlinLogging.logger("sim-broker-test")

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {

    // receives the newly created orders by the policy and processes them
    val broker = setupSimBroker()

    // the engine of the platform that performs the actual run and orchestrates the interaction
    // between the components
    val roboquant = setupRoboquant(broker)

    // default avro feed with SP500 daily PriceBar data (only for testing)
    val feed = AvroFeed.sp500()

    // run the simulation,
    roboquant.run(feed)
  }

  private fun setupSimBroker(): Broker {
    logger.info { "setting up broker..." }

    // how much to initially deposit into the account
    val initialDeposit = Wallet(20_000.USD)

    // currency to use for reporting
    val baseCurrency = Currency.USD

    // model to use to calculate fees, commissions, etc
    val feeModel = PercentageFeeModel(fee = 0.01)

    // type of account to model, like a Cash or Margin account
    val accountModel = CashAccount(minimum = 100.0)

    // engine to use to calculate the final price of a trade
    val pricingEngine = SpreadPricingEngine(spread = 5.bips, priceType = "OPEN")

    return SimBroker(
      initialDeposit = initialDeposit,
      baseCurrency = baseCurrency,
      feeModel = feeModel,
      accountModel = accountModel,
      pricingEngine = pricingEngine,
    )
  }

  private fun setupRoboquant(broker: Broker): Roboquant {
    logger.info { "setting up roboquant..." }

    // receives the event and generates zero or more signals
    val emaStrategy = EMAStrategy()
    val rsiStrategy = RSIStrategy()
    val strategy = ParallelStrategy(emaStrategy, rsiStrategy)

    // receives the generated signals and creates the actual orders
    val policy = setupPolicy()

    // receives the latest state of the account and calculates various metrics that are of interest
    // to determine the progress and results of the run so far
    val metrics = listOf(AccountMetric(), PNLMetric())

    // how metrics are stored / logged
    val logger = InfoLogger()

    return Roboquant(
      strategy = strategy,
      metrics = metrics,
      policy = policy,
      broker = broker,
      logger = logger,
    )
  }

  private fun setupPolicy(): Policy = FlexPolicy {
    orderPercentage = 0.01
    shorting = true
    priceType = "OPEN"
    fractions = 4
    oneOrderOnly = false
    safetyMargin = 0.1
    minPrice = 10.USD
  }
}
