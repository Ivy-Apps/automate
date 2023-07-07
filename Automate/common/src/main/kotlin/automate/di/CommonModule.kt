package automate.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module

@Module
@ContributesTo(AppScope::class)
object CommonModule {
//    @Provides
//    @JvmStatic
//    @SingleIn(AppScope::class)
//    @AppCoroutineScope
//    fun appCoroutineScope(): CoroutineScope {
//        return CoroutineScope(CoroutineName("App scope"))
//    }
}