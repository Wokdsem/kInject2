package com.wokdsem.kinject.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

internal fun compile(vararg sources: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        inheritClassPath = true
        kspWithCompilation = true
        this.sources = sources.toList()
        symbolProcessorProviders = listOf(KinjectProcessorProvider())
    }.compile()
}

@Suppress("UNCHECKED_CAST")
internal fun getCompilation(graph: SourceFile, graphName: String, `package`: String = "", override: String? = null): Compilation {
    compile(graph).classLoader.run {
        val packagePrefix = if (`package`.isEmpty()) `package` else "$`package`."
        val testGraphClass = loadClass("$packagePrefix$graphName").kotlin
        val kTestGraphClass = loadClass("${packagePrefix}K$graphName").kotlin
        val overrideClass = override?.let { loadClass("${packagePrefix}${it}").kotlin }
        val testGraphInstance = checkNotNull(value = testGraphClass.primaryConstructor).call()
        val overrideInstance = overrideClass?.let { checkNotNull(value = it.primaryConstructor).call() }
        val kGraphInstance = with(checkNotNull(kTestGraphClass.companionObject)) {
            checkNotNull(declaredFunctions.first { it.name == "from" }.call(objectInstance, testGraphInstance, overrideInstance))
        }
        return object : Compilation {
            private fun depOf(kClass: KClass<*>, instance: Any, dep: String) = (kClass.declaredMemberProperties.first { it.name == dep } as KProperty1<Any, *>).get(instance)
            override fun getDep(dep: String) = depOf(testGraphClass, testGraphInstance, dep)
            override fun getKDep(dep: String) = depOf(kTestGraphClass, kGraphInstance, dep)
        }
    }
}

internal fun asserCompilationError(graph: SourceFile, expectedGraph: String, errorMessage: String) {
    compile(graph).run {
        try {
            classLoader.loadClass(expectedGraph)
            org.junit.jupiter.api.fail("Unexpected successful compilation")
        } catch (e: Throwable) {
            assert(value = errorMessage in messages)
        }
    }
}

internal interface Compilation {
    fun getDep(dep: String): Any?
    fun getKDep(dep: String): Any?
}