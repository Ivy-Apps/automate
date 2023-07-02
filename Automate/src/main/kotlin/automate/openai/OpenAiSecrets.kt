package automate.openai

import automate.di.AppScope
import automate.di.SingleIn
import javax.inject.Inject

@SingleIn(AppScope::class)
class OpenAiSecrets @Inject constructor() {
    val apiKey by lazy {
        requireNotNull(
            System.getProperty("OPEN_AI_API_KEY", null)
                .takeIf { it.isNotBlank() }
        ) {
            """
                The OPEN_AI_API_KEY is null. 
                You must setup it in your ~/.gradle/gradle.properties as
                "ivyapps_openai_api_key".
            """.trimIndent()
        }
    }
}