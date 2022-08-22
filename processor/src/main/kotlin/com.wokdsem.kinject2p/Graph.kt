package com.wokdsem.kinject2p

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

internal class Graph(val root: KSClassDeclaration, val files: List<KSFile>, val providers: List<Provider>)

@JvmInline
internal value class Id(val id: String)
internal class Provider(
    val id: Id,
    val type: KSType,
    val exported: Boolean,
    val scope: Scope,
    val isNullable: Boolean,
    val providerFunction: String,
    val dependencies: List<Dependency>,
    val declaration: KSFunctionDeclaration
)

internal class Dependency(val id: Id, val name: String, val isNullable: Boolean)
internal enum class Scope { FACTORY, EAGER, SINGLE }

