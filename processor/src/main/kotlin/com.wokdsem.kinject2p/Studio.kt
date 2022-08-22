package com.wokdsem.kinject2p

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun processGraph(graphDeclaration: KSClassDeclaration): Analysis<Graph> {
    val graphFile = graphDeclaration.containingFile ?: return fail("Unexpected non-backed file class", graphDeclaration)
    return graphDeclaration.validateGraphDeclaration().flatMap { graph ->
        val files = mutableListOf(graphFile)
        val providersIndex = mutableMapOf<Id, Provider>()
        graph.getDeclaredFunctions().forEach { method ->
            method.indexProvider().getOr { failure -> return failure }?.let { provider ->
                val previousValue = providersIndex.put(provider.id, provider)
                if (previousValue != null) {
                    val failMessage = "Providers clash, a dependency type can only be provided once - typealias may help to break the clash"
                    return fail(failMessage, provider.declaration, previousValue.declaration)
                }
            }
        }
        val providers = validateGraph(providersIndex).getOr { failure -> return failure }
        Graph(root = graphDeclaration, files = files, providers = providers).success
    }
}

private fun validateGraph(graph: Map<Id, Provider>): Analysis<List<Provider>> {
    val success = true.success
    val validated = mutableSetOf<Id>()
    val providers = mutableListOf<Provider>()
    fun validateProvider(provider: Provider, path: Set<Id> = emptySet()): Analysis<Boolean> {
        provider.dependencies.forEach { dep ->
            fun cycleList() = (path.toList() + provider.id + dep.id).joinToString(separator = " -> ") { dep -> dep.id.takeLastWhile { it != '.' } }
            val isValidated = dep.id in validated
            if (!isValidated && dep.id in path) return fail("Graph cycle detected ${cycleList()}", graph.getValue(dep.id).declaration)
            if (dep.isNullable && isValidated) return@forEach
            val depProvider = graph.getOrElse(dep.id) { return fail("The provider for dependency ${dep.name} is missing", provider.declaration) }
            if (!dep.isNullable && depProvider.isNullable) return fail("The provider for dependency ${dep.name} requires that ${dep.name} to be nullable", provider.declaration)
            if (!isValidated) validateProvider(depProvider, path + provider.id).getOr { failure -> return failure }
        }
        validated += provider.id
        providers += provider
        return success
    }
    graph.values.forEach { provider -> if (provider.id !in validated) validateProvider(provider).getOr { failure -> return failure } }
    return providers.success
}

private fun KSFunctionDeclaration.indexProvider(): Analysis<Provider?> {
    val returnType = checkNotNull(returnType?.resolve())
    if (returnType.isMarkedNullable) return null.success
    fun toProvider(exported: Boolean, scope: Scope): Analysis<Provider> {
        val dependencies = parameters.map { param -> param.indexProviderDependency().getOr { return it } }
        return validateProviderDeclaration().map {
            val dep = checkNotNull(returnType.arguments.first().type).resolve()
            Provider(
                id = dep.id,
                type = dep, exported = exported, scope = scope, isNullable = dep.isMarkedNullable,
                providerFunction = simpleName.asString(), dependencies = dependencies, declaration = this
            )
        }
    }
    return when (returnType.declaration.qualifiedName?.asString()) {
        FACTORY -> toProvider(false, Scope.FACTORY)
        SINGLE -> toProvider(false, Scope.SINGLE)
        EAGER -> toProvider(false, Scope.EAGER)
        EXPORTED_FACTORY -> toProvider(true, Scope.FACTORY)
        EXPORTED_SINGLE -> toProvider(true, Scope.SINGLE)
        EXPORTED_EAGER -> toProvider(true, Scope.EAGER)
        else -> null.success
    }
}

private fun KSValueParameter.indexProviderDependency(): Analysis<Dependency> {
    return validateProviderDependency().map { dep ->
        with(receiver = dep.type.resolve()) { Dependency(id = id, name = checkNotNull(name).asString(), isNullable = isMarkedNullable) }
    }
}

private val KSType.id get() = Id(id = (if (isMarkedNullable) makeNotNullable() else this).toTypeName().toString())

private fun KSClassDeclaration.validateGraphDeclaration(): Analysis<KSClassDeclaration> {
    fun error(message: String): Analysis<KSClassDeclaration> = fail(message, this)
    if (classKind != ClassKind.CLASS) return error("Only classes can be annotated as Graphs")
    if (superTypes.any { it.toTypeName().toString() != "kotlin.Any" }) return error("A graph cannot extend other classes or implement any interfaces")
    if (getVisibility().let { visibility -> visibility != Visibility.PUBLIC && visibility != Visibility.INTERNAL }) return error("Only public or internal visibility modifiers are allowed")
    if (typeParameters.isNotEmpty()) return error("A graph cannot be parametrized with generic types")
    return success
}

private fun KSFunctionDeclaration.validateProviderDeclaration(): Analysis<KSFunctionDeclaration> {
    fun error(message: String): Analysis<KSFunctionDeclaration> = fail(message, this)
    if (extensionReceiver != null) return error("Extension are not allowed for a dependency provider")
    if (getVisibility().let { visibility -> visibility != Visibility.PUBLIC && visibility != Visibility.INTERNAL }) return error("Only public or internal visibility modifiers are allowed")
    if (Modifier.SUSPEND in modifiers) return error("Suspend function are not allowed for a dependency provider")
    if (typeParameters.isNotEmpty()) return error("A provider cannot be parametrized with generic types")
    return success
}

private fun KSValueParameter.validateProviderDependency(): Analysis<KSValueParameter> {
    fun error(message: String): Analysis<KSValueParameter> = fail(message, this)
    if (hasDefault) return error("Default values are not allowed")
    if (isVararg) return error("Vararg param is not allowed")
    return success
}
