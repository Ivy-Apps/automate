package automate

import automate.openai.chatgpt.normalizePrompt

object Constants {
    const val MODEL_LABEL =
        "Walter White, ''Heisenberg' from Breaking bad"
    val ARTICLE_TOPIC = """
7 reasons why Javascript sucks and should be deleted for good.
""".normalizePrompt()
    const val ARTICLE_TARGET_WORDS_COUNT = 700
    const val MIN_SECTIONS_COUNT = 2

    val ARTICLE_REQUIREMENTS = """      
- Talk like Walter White from Breaking Bad from 1st person.
- Say that you're Walter White in the beginning.
- Use the iconic "You're god damn, right!" and "I am the danger!!!" phrases
- Avoid duplicating content at any cost in the article.
- Make it fun! 
- Make Breaking Bad jokes. Keep it short.
- Do NOT talk about "meth" or drugs.
- Aim for approx $ARTICLE_TARGET_WORDS_COUNT words.
""".normalizePrompt()


    const val MAX_ACTIVE_ERRORS = 10
    const val MAX_ERRORS = 25
    const val MAX_STEPS = 50
}