package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.symbol.*

internal class Graph(val root: KSClassDeclaration, val files: List<KSFile>, val modules: List<Module>, val providers: List<Provider>, val exporters: List<Exporter>)

@JvmInline
internal value class Id(val id: String)

internal class Module(val id: Id, val node: KSType, val source: Id, val declaration: KSFunctionDeclaration)
internal class Provider(val id: Id, val scope: Scope, val node: KSType, val dependencies: List<Dependency>, val source: Id, val declaration: KSFunctionDeclaration)
internal class Exporter(val id: Id, val node: KSType, val type: Type, val declaration: KSFunctionDeclaration) {
    sealed interface Type {
        object Delegated : Type
        class Bracket(val dependencies: List<Dependency>) : Type
    }
}

internal class Dependency(val id: Id, val node: KSType, val name: String, val isNullable: Boolean, val declaration: KSNode)
internal enum class Scope { FACTORY, EAGER, SINGLE }

internal val Module.reference get() = declaration.reference
internal val Provider.reference get() = declaration.reference
internal val Provider.isNullable get() = node.isMarkedNullable
internal val Exporter.reference get() = declaration.reference
private val KSFunctionDeclaration.reference get() = simpleName.asString()