package automate.openai.chatgpt.data

data class ChatGptAgentError(val error: String) : Exception(error)