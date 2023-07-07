package automate.di

import automate.AppCoroutineScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope

@Module
@ContributesTo(AppScope::class)
object CommonModule {
    @Provides
    @SingleIn(AppScope::class)
    @AppCoroutineScope
    fun appCoroutineScope(): CoroutineScope {
        return CoroutineScope(CoroutineName("App scope"))
    }
}