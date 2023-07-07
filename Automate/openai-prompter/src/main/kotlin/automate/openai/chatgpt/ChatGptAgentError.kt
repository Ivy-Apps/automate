package automate.openai.chatgpt

data class ChatGptAgentError(val error: String) : Exception(error)