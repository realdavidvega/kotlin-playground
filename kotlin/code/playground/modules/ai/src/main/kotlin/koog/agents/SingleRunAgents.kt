package koog.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import kotlinx.coroutines.runBlocking

/**
 * (1) Single-run agents
 *
 * The AIAgent class is the core component that lets you create AI agents in your Kotlin
 * applications.
 *
 * You can build simple agents with minimal configuration or create sophisticated agents with
 * advanced capabilities by defining custom strategies, tools, configurations, and custom
 * input/output types.
 *
 * This page guides you through the steps necessary to create a single-run agent with customizable
 * tools and configurations.
 *
 * A single-run agent processes a single input and provides a response. It operates within a single
 * cycle of tool-calling to complete its task and provide a response. This agent can return either a
 * message or a tool result. The tool result is returned if the tool registry is provided to the
 * agent.
 *
 * If your goal is to build a simple agent to experiment with, you can provide only a prompt
 * executor and LLM when creating it. But if you want more flexibility and customization, you can
 * pass optional parameters to configure the agent
 */
object SingleRunAgents {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      // Create a single-run agent
      val agent = AIAgent(
        executor = simpleGoogleAIExecutor(System.getenv("GOOGLE_API_KEY")),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        llmModel = GoogleModels.Gemini2_5Flash,
        temperature = 0.0, // Temperature controls the randomness of the agent's responses
      )

      val result = agent.run("Hello! How can you help me?")
      println(result)
      println("-------------------------------------")

      // You can use the built-in tools or implement your own custom tools if needed.
      val toolRegistry = ToolRegistry {
        // SayToUser is the built-in tool, which ends up doing a println of the message
        tool(SayToUser)
      }

      // Create an agent with a model compatible with tools
      val toolAgent =
        AIAgent(
          executor = simpleOpenAIExecutor(System.getenv("OPENAI_API_KEY")),
          systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
          llmModel = OpenAIModels.Chat.GPT4o,
          toolRegistry = toolRegistry, // Pass the tool registry
          temperature = 0.0,
          maxIterations = 30 // Maximum number of iterations to run the agent
        ) {
          // Single-run agents support custom event handlers.
          // While having an event handler is not required for creating an agent,
          // it might be helpful for testing, debugging, or making hooks for chained agent
          // interactions.

          handleEvents {
            // Handle tool calls
            onToolCall { eventContext ->
              println("Tool called: ${eventContext.tool} with args ${eventContext.toolArgs}")
            }

            // Handle event triggered when the agent completes its execution
            onAgentFinished { eventContext ->
              println("Agent finished with result: ${eventContext.result}")
            }
          }
        }

      toolAgent.run("Hello! How can you help me?")
      println("-------------------------------------")
    }
  }
}
