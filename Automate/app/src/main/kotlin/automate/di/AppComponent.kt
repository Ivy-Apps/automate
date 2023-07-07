package automate.di

import automate.AutomateApp
import com.squareup.anvil.annotations.MergeComponent

@MergeComponent(
    scope = AppScope::class,
    modules = [
        CommonModule::class,
    ]
)
@SingleIn(AppScope::class)
interface AppComponent {
    fun automateApp(): AutomateApp
}