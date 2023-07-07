package automate.linkedinpost

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import automate.openai.chatgpt.data.ChatGptParams
import automate.openai.chatgpt.di.ChatGptAgentFactory
import automate.statemachine.stateMachine
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProgrammerYodaLinkedInPostAgent @Inject constructor(
    private val agentFactory: ChatGptAgentFactory,
) {
    private val coroutineScope = CoroutineScope(CoroutineName("Scope"))

    companion object {
        const val HOOK = "hook"
        const val OUTLINE = "outline"
        const val POST_BODY = "post-body"
        const val HASHTAGS = "#hashtags"
    }

    suspend fun producePost(
        topic: String,
        requirements: NonEmptyList<String>
    ): String {
        val agent = agentFactory.create(
            params = ChatGptParams(
                goal = "Write a fun LinkedIn post on '$topic'.",
                requirements = requirements + nonEmptyListOf(
                    "Talk like Yoda.",
                    "Teach like Yoda.",
                ),
                behavior = "Grandmaster Yoda from Star Wars who also invented computer programming",
            ),
            stateMachine = stateMachine
        )
        coroutineScope.launch {
            agent.dataFlow.collectLatest { data ->
                println("------------------")
                println(data)
                println("------------------")
                println()
            }
        }
        println("Completion: ${agent.run()}")
        val data = agent.dataFlow.value
        return buildString {
            append(data[HOOK])
            append("\n\n")
            append(data[POST_BODY])
            append("\n\n")
            append(data[HASHTAGS])
        }
    }

    private val stateMachine = stateMachine(
        maxSteps = 30,
        maxErrors = 10,
    ) {
        initialState("hook") {
            transition(
                name = "Hook",
                description = "Write an engaging first words on the topic.",
                inputs = {
                    input(HOOK, "Engage the audience like Yoda. Max 50 chars.")
                }
            ) {
                val hook = input(HOOK)
                if (hook.length > 50) {
                    error("The hook must be 50 characters or less.")
                }
                data[HOOK] = hook
                goTo("improve-hook")
            }
        }

        state("improve-hook") {
            transition(
                name = "Edit hook",
                description = "Make the hook more fun. Make it sound more like Yoda.",
                inputs = {
                    input(HOOK, "More engaging and fun hook.")
                }
            ) {
                data["hook"] = input(HOOK)
                goTo("outline")
            }
        }

        state("outline") {
            transition(
                name = "outline",
                description = "List 5-7 points to be made in the post.",
                inputs = {
                    input(OUTLINE, "The core points of the post")
                }
            ) {
                data[OUTLINE] = input(OUTLINE)
                goTo("post-body")
            }
        }

        state("post-body") {
            transition(
                name = "post-body",
                description = "Write the post body. Use the outline. Keep it short and fun.",
                inputs = {
                    input(
                        POST_BODY,
                        "Write a sentence or two for each point in the outline."
                    )
                }
            ) {
                data["post-body"] = input(POST_BODY)
                goTo("edit-1")
            }
        }

        state("edit-1") {
            transition(
                name = "edit-1",
                description = "Edit the post body. Remove duplicated content. Follow the outline.",
                inputs = {
                    input(
                        POST_BODY,
                        "An updated post-body without duplicated content matching the outline."
                    )
                }
            ) {
                data["post-body"] = input(POST_BODY)
                goTo("finalize")
            }
        }

        state("finalize") {
            transition(
                name = "final-edit",
                description = """
                    Finalize the post. Make sure that:
                    - It's on the topic.
                    - Sounds like Yoda.
                    - There's no duplicated content.
                    - It's short, informative and fun.
                    
                    Add hashtags for better reach.
                """.trimIndent(),
                inputs = {
                    input(
                        HOOK,
                        "The final version on the hook for the post. It must be short and engaging."
                    )
                    input(
                        POST_BODY,
                        "The final version of the post body."
                    )
                    input(
                        HASHTAGS,
                        "Optimal LinkedIn #hashtags on the topic to improve the post's reach."
                    )
                }
            ) {
                val hook = input(HOOK)
                if (hook.length > 60) {
                    error("The final hook must be 60 characters or less.")
                }
                data[HOOK] = hook
                data[POST_BODY] = input(POST_BODY)
                data[HASHTAGS] = input(HASHTAGS)
                goTo("done")
            }
        }

        finalState("done")
    }
}