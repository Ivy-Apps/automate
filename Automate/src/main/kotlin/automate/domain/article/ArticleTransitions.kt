package automate.domain.article

import arrow.core.Either
import arrow.core.raise.either
import automate.data.ModelFeedback.Error
import automate.data.ModelFeedback.Suggestion
import automate.domain.article.data.Article
import automate.domain.article.data.BodyItem
import automate.statemachine.InputMap
import automate.statemachine.Transition
import automate.statemachine.TransitionParam

sealed class ArticleTransition : Transition<ArticleState, Article>()

object SetTitleTransition : ArticleTransition() {
    override val name = "Set article title"
    override val description = "Select an engaging title."

    val PARAM_TITLE = TransitionParam(
        name = "title",
        description = "Up to 100 chars long article title.",
        tips = listOf(
            "Target length: around 60 chars.",
            "Use symbols like '[]', ':', '-' for better formatting.",
            "Be creative. Don't copy the topic 1:1."
        ),
        type = String::class,
    )

    override val input = listOf(PARAM_TITLE)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val title = requiredParam(input, PARAM_TITLE)
        ArticleState.WriteIntroduction(
            data = state.data.copy(title = title)
        ) to emptyList()
    }
}

object WriteIntroduction : ArticleTransition() {
    override val name: String = "Compose the introduction"
    override val description: String = "Explain what the article is about"

    private val PARAM_INTRODUCTION = TransitionParam(
        name = "introduction",
        type = String::class,
        description = "Up to 30 words",
        tips = listOf(
            "Make it fun and engaging"
        ),
    )

    override val input: List<TransitionParam<*>> = listOf(PARAM_INTRODUCTION)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val introduction = requiredParam(input, PARAM_INTRODUCTION)
        ArticleState.WriteBody(
            data = state.data.copy(
                introduction = introduction,
            )
        ) to emptyList()
    }

}

object AddSectionTransition : ArticleTransition() {
    override val name = "Add a section"
    override val description = "Create a point in the article with a title and one or more paragraphs."

    val PARAM_TITLE = TransitionParam(
        name = "title",
        description = "The title of the article section. It must be an unique section.",
        type = String::class,
        tips = listOf(
            "Do NOT duplicate already existing section titles!"
        )
    )
    val PARAM_TEXT = TransitionParam(
        name = "text",
        description = "The text of the article section. Give examples.",
        type = String::class,
        tips = listOf(
            "Supports Markdown.",
            "Code examples are a must!",
            "Keep it short, straightforward and fun."
        )
    )

    override val input = listOf(PARAM_TITLE, PARAM_TEXT)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val title = requiredParam(input, PARAM_TITLE)
        val text = requiredParam(input, PARAM_TEXT)

        val article = state.data
        val sections = article.sectionsTitles()
        val duplicatedSection = sections.any { sectionTitle ->
            sectionTitle.equals(title, ignoreCase = true)
        }
        if (duplicatedSection) {
            raise(
                Error("Section with a title '$title' already exists. Add a new unique section.")
            )
        }

        val newSection = BodyItem.Section(title = title, text = text)
        ArticleState.WriteBody(
            data = article.copy(
                body = state.data.body + newSection
            )
        ) to emptyList()
    }
}

object WriteConclusionTransition : ArticleTransition() {
    override val name: String = "Compose a conclusion"
    override val description = "Wrap up the article with a summary or key takeaways."

    private val PARAM_CONCLUSION = TransitionParam(
        name = "conclusion",
        type = String::class,
        description = "Summarize the article in the conclusion.",
        tips = null,
    )

    override val input: List<TransitionParam<*>> = listOf(PARAM_CONCLUSION)

    override fun transition(
        state: ArticleState,
        input: InputMap
    ): Either<Error, Pair<ArticleState, List<Suggestion>>> = either {
        val conclusion = requiredParam(input, PARAM_CONCLUSION)
        ArticleState.Conclusion(
            state.data.copy(
                conclusion = conclusion,
            )
        ) to emptyList()
    }
}