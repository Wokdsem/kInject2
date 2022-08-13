package com.wokdsem.kinject2p

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal class Graph(val root: KSClassDeclaration, val files: List<KSFile>, val providers: Map<DepId, Provider>)

@JvmInline
internal value class DepId(val id: String)
internal class Provider(val id: DepId, val exported: Boolean, val scope: Scope, val isNullable: Boolean, val dependencies: List<Dependency>, val declaration: KSFunctionDeclaration)
internal class Dependency(val id: DepId, val name: String, val isNullable: Boolean)
internal enum class Scope { FACTORY, EAGER, SINGLE }

