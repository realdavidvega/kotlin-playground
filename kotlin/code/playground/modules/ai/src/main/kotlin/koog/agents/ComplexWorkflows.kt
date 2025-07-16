package koog.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgent.FeatureContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.feature.handler.AgentFinishedContext
import ai.koog.agents.core.feature.handler.AgentStartContext
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import kotlinx.coroutines.runBlocking

/**
 * (2) Complex Agent Workflows
 *
 * In addition to single-run agents, the AIAgent class lets you build agents that handle complex
 * workflows by defining custom strategies, tools, configurations, and custom input/output types.
 *
 * The process of creating and configuring such an agent typically includes the following steps:
 * 1. Provide a prompt executor to communicate with the LLM.
 * 2. Define a strategy that controls the agent workflow.
 * 3. Configure agent behavior.
 * 4. Implement tools for the agent to use.
 * 5. Add optional features like event handling, memory, or tracing.
 * 6. Run the agent with user input.
 */
object ComplexWorkflows {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // First, let's create a prompt executor that works with multiple LLM providers
      // For that, create the clients for the required LLM providers with the corresponding API keys
      val openAIClient = OpenAILLMClient(System.getenv("OPENAI_API_KEY"))
      val googleClient = GoogleLLMClient(System.getenv("GOOGLE_API_KEY"))

      // Then, pass the configured clients to the DefaultMultiLLMPromptExecutor class constructor
      // to create a prompt executor with multiple LLM providers:
      val promptExecutor =
        MultiLLMPromptExecutor(
          LLMProvider.OpenAI to openAIClient,
          LLMProvider.Google to googleClient,
        )

      // Create a strategy, which defines the workflow of your agent by using nodes and edges.
      // It can have arbitrary input and output types, which can be specified in strategy function
      // generic parameters. These will be input/output types of the AIAgent as well.
      // Default type for both input and output is String.
      val agentStrategy =
        strategy<String, String>("Simple calculator") {
          // We have to define the nodes and edges of the strategy, which are the building blocks of
          // the strategy

          // The strategy (or graph) looks something like this:
          // (Start) -> (Send input) : The graph starts by sending the user input to the agent
          // (Send input) -> (Finish) : If the output is an assistant message, the agent finishes
          // (Send input) -> (Execute tool) : If the output is a tool call, the agent executes the
          // tool
          // (Execute tool) -> (Send the tool result) : The agent sends the tool result to next step
          // (Send the tool result) -> (finish) : The agent finishes by sending the tool result to
          // the user

          // Nodes represent processing steps in your agent strategy
          // This node will be used to append a user message to the LLM prompt and
          // get a response with optional tool usage.
          val nodeSendInput by nodeLLMRequest()

          // This node will be used to execute a tool call and return its result
          val nodeExecuteTool by nodeExecuteTool()

          // This node will be used to add a tool result to the prompt and request an LLM response
          val nodeSendToolResult by nodeLLMSendToolResult()

          // Edges represent connections between nodes
          // (Start) -> (Send input)
          edge(nodeStart forwardTo nodeSendInput)

          // (Send input) -> (Finish)
          // This is an edge with an event handler on assistant message and a transformer
          // Edges can have conditions too
          edge(
            (nodeSendInput forwardTo nodeFinish)
            // Transform the output before passing it to the target node
            transformed
              {
                it
              }
              // Filter based on the assistant message
              onAssistantMessage
              {
                true
              }
          )

          // (Send input) -> (Execute tool)
          edge(
            (nodeSendInput forwardTo nodeExecuteTool)
            // Filter based on the tool call
            onToolCall { true }
          )

          // (Execute tool) -> (Send the tool result)
          edge(nodeExecuteTool forwardTo nodeSendToolResult)

          // (Send the tool result) -> (finish)
          // This an edge with
          edge(
            (nodeSendToolResult forwardTo nodeFinish) transformed { it } onAssistantMessage { true }
          )
        }

      // The strategy function lets you define multiple sub-graphs, each containing its own set
      // of nodes and edges. This approach offers more flexibility and functionality compared to
      // using simplified strategy builders

      // Now let's implement tools and set up a tool registry
      // Tools let your agent perform specific tasks. To make a tool available for the agent,
      // add it to a tool registry.

      // For instance, we implement a simple calculator tool that can add two numbers
      @LLMDescription("Tools for performing basic arithmetic operations")
      class CalculatorTools : ToolSet {
        @Tool
        @LLMDescription("Add two numbers together and return their sum")
        fun add(
          @LLMDescription("First number to add (integer value)") num1: Int,
          @LLMDescription("Second number to add (integer value)") num2: Int,
        ): String {
          val sum = num1 + num2
          return "The sum of $num1 and $num2 is: $sum"
        }
      }

      // Add the tool to the tool registry
      val toolRegistry = ToolRegistry { tools(CalculatorTools()) }

      // Now we can install the features, which let you add new capabilities to the agent,
      // modify its behavior, provide access to external systems and resources, and log and monitor
      // events while the agent is running. The following features are available:
      // - EventHandler
      // - AgentMemory
      // - Tracing

      // To install the feature, call the 'install' function and provide the feature as an argument.
      val installFeatures: FeatureContext.() -> Unit = {
        install(EventHandler) {
          // Append handler called when an agent is started
          onBeforeAgentStarted { eventContext: AgentStartContext<*> ->
            println("Starting strategy: ${eventContext.strategy.name}")
          }
          // Append handler called when an agent finishes execution.
          onAgentFinished { eventContext: AgentFinishedContext ->
            println("Result: ${eventContext.result}")
          }
        }
      }

      // Now we define an agent configuration
      val agentConfig =
        AIAgentConfig(
          prompt =
            Prompt.build("simple-calculator") {
              system(
                """
                You are a simple calculator assistant.
                You can add two numbers together using the calculator tool.
                When the user provides input, extract the numbers they want to add.
                The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
                Extract the two numbers and use the calculator tool to add them.
                Always respond with a clear, friendly message showing the calculation and result.
                """
                  .trimIndent()
              )
            },
          model = OpenAIModels.Chat.GPT4o, // Use GPT-4o
          maxAgentIterations = 10, // Maximum number of agent iterations to complete the task
        )

      val agent =
        AIAgent(
          promptExecutor = promptExecutor,
          toolRegistry = toolRegistry,
          strategy = agentStrategy,
          agentConfig = agentConfig,
          installFeatures = installFeatures,
        )

      println("Enter two numbers to add (e.g., 'add 5 and 7' or '5 + 7'):")

      // Read the user input and send it to the agent
      val userInput = readlnOrNull() ?: ""
      val agentResult = agent.run(userInput)
      println("The agent returned: $agentResult")
    }
  }
}
