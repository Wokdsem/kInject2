package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.*

internal class Graph(val root: KSClassDeclaration, val files: List<KSFile>, val modules: List<Module>, val providers: Map<Id, Provider>, val exports: List<Export>)

@JvmInline
internal value class Id(val id: String)

internal class Module(val id: Id, val node: KSType, val providers: List<Id>, val imports: List<Id>, val source: Id, val declaration: KSFunctionDeclaration)
internal class Provider(
    val id: Id, val scope: Scope, val exported: Boolean, val node: KSType, val dependencies: List<Dependency>, val source: Id, val declaration: KSFunctionDeclaration
)

internal class Export(val id: Id, val node: KSType, val type: Type, val declaration: KSFunctionDeclaration) {
    sealed interface Type {
        data object Delegated : Type
        class Bracket(val dependencies: List<Dependency>) : Type
    }
}

internal class Dependency(val id: Id, val node: KSType, val name: String, val isNullable: Boolean, val declaration: KSNode)
internal enum class Scope { FACTORY, EAGER, SINGLE }

@OptIn(KspExperimental::class)
internal val Graph.name get() = root.getAnnotationsByType(com.wokdsem.kinject.Graph::class).first().name
internal val Module.reference get() = declaration.reference
internal val Provider.reference get() = declaration.reference
internal val Provider.isNullable get() = node.isMarkedNullable
internal val Export.reference get() = declaration.reference
private val KSFunctionDeclaration.reference get() = simpleName.asString()