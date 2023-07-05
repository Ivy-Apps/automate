package automate.openai.chatgpt

import automate.openai.normalizePrompt
import javax.inject.Inject

class ChatGptPrompter @Inject constructor(
    private val agent: ChatGptAgent,
    private val api: ChatGptApi,
) {
    companion object {
        fun startTag(name: String) = "`[${name.uppercase()}_START]`"
        fun endTag(name: String) = "`[${name.uppercase()}_END]`"
    }

    fun prePrompt(): String = """
You are a pattern-following assistant that lives in a state-machine
to achieve the following goal: "${agent.goal}" by completing the task.
The task requirements are:
- ${agent.requirements.joinToString("\n- ")}
 
You're role is '${agent.behavior}' and must role-play this behavior.

You must only respond with in this format:
- Sections start with ${startTag("sectionName")} and ends with ${endTag("sectionName")}.
- On each message you'll receive:
1. ${startTag("state")}: The state of the task
2. ${startTag("choices")} with ${startTag("option 1")}, 
${startTag("option 2")}, ${startTag("option N")} inside them.
3. Each ${startTag("option X")} has zero or many "Inputs".
- You must respond by inspecting ${startTag("state")} and responding with
the best ${startTag("option X")} providing the proper string inputs
to complete the task and achieve the goal.
4. An option inside ${startTag("choices")} looks like this:
${startTag("Option 1")}
A description of the option and what it does.
Inputs:
- "a": A variable named "a" that does...
- "b": Lorem ipsum
- "c": Lorem ipsum
${endTag("Option 1")}
5. After analyzing the ${startTag("state")} and choosing the best option you
must respond with:
${startTag("Option X")}
- "a": This is a start of the value of the variable "a"
- "b": Value for "b"
- "c": Value for "c"
${endTag("Option X")}
6. If you make an invalid or a bad choice you'll receive an additional ${startTag("error")}
with information what went wrong. You must adjust your next choice based on the information
in the ${startTag("error")}.
""".normalizePrompt()

}