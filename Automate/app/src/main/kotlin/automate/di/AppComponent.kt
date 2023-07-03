package automate.di

import automate.AutomateApp
import com.squareup.anvil.annotations.MergeComponent

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface AppComponent {
    fun automateApp(): AutomateApp
}