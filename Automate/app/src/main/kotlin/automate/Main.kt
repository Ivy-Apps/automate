package automate

import automate.di.DaggerAppComponent

suspend fun main(args: Array<String>) {
    val appComponent = DaggerAppComponent.create()
    val app = appComponent.automateApp()
    app.run()
}