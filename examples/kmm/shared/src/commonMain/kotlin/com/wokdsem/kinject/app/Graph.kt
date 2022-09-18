package com.wokdsem.kinject.app

import com.wokdsem.kinject.Graph
import com.wokdsem.kinject.export.export
import com.wokdsem.kinject.scope.single

@Graph
internal class SharedModuleGraph {
    fun provideKInjectVersion() = single { "kInject 2.X.X" }
    fun providePlatform() = single { Platform() }
    fun exportHolder() = export<GreetHolder>()
}
