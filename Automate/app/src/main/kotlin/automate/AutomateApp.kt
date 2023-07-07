package automate

import arrow.core.nonEmptyListOf
import automate.linkedinpost.ProgrammerYodaLinkedInPostAgent
import javax.inject.Inject

class AutomateApp @Inject constructor(
    private val programmerYodaLinkedInPostAgent: ProgrammerYodaLinkedInPostAgent
) {

    suspend fun run() {
        val post = programmerYodaLinkedInPostAgent.producePost(
            topic = "Teaching all core concepts in programming.",
            requirements = nonEmptyListOf(
                "Do it in a fun and wise way.",
                "Teach the core concepts like variables, types, branching, functions.",
                "Teach all the concepts that makes programming possible.",
            )
        )

        println()
        println()
        println("--------------------")
        println(post)
    }
}