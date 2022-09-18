package com.wokdsem.kinject.app

internal interface GreetHolder {
    val kInjectVersion: String
    val platform: Platform
}

object Greeting {

    private val holder: GreetHolder by lazy {
        KSharedModuleGraph.from(graph = SharedModuleGraph()).greetHolder
    }

    fun greeting(): String {
        return "Hello, ${holder.platform.platform} running ${holder.kInjectVersion}"
    }
}