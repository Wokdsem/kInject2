package com.wokdsem.kinject2p

import com.google.devtools.ksp.symbol.*

internal class Graph(val root: KSClassDeclaration, val files: List<KSFile>, val providers: List<Provider>, val exporters: List<Exporter>)

@JvmInline
internal value class Id(val id: String)

internal class Provider(val id: Id, val node: KSType, val dependencies: List<Dependency>, val scope: Scope, val declaration: KSFunctionDeclaration)
internal class Exporter(val id: Id, val node: KSType, val type: Type, val declaration: KSFunctionDeclaration) {
    sealed interface Type {
        object Delegated : Type
        class Bracket(val dependencies: List<Dependency>) : Type
    }
}

internal class Dependency(val id: Id, val node: KSType, val name: String, val isNullable: Boolean, val declaration: KSNode)
internal enum class Scope { FACTORY, EAGER, SINGLE }

internal val Provider.reference get() = declaration.simpleName.asString()
internal val Provider.isNullable get() = node.isMarkedNullable
internal val Exporter.reference get() = declaration.simpleName.asString()
