package com.wokdsem.kinject2p

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.wokdsem.kinject2p.Exporter.Type.Bracket
import com.wokdsem.kinject2p.Statement.Declaration

internal fun processGraphDeclaration(graphDeclaration: KSClassDeclaration): Analysis<Graph> {
    return graphDeclaration.collectGraph()
        .validate { rawGraph -> validateExporters(rawGraph.exporters, rawGraph.providersIndex) }
        .validate { rawGraph -> validateProvidersGraph(rawGraph.providersIndex) }
        .map { rawGraph -> with(rawGraph) { Graph(root = root, files = files, providers = providersIndex.values.toList(), exporters = exporters.values.toList()) } }
}

private fun KSClassDeclaration.collectGraph(): Analysis<RawGraph> {
    val graphFile = containingFile ?: return fail("Unexpected non-backed file class", this)
    return validateGraphDeclaration().flatMap { graph ->
        val files = mutableListOf(graphFile)
        val providersIndex = mutableMapOf<Id, Provider>()
        val exportersIndex = mutableMapOf<Id, Exporter>()

        fun KSClassDeclaration.process(): Analysis<Unit> {

            fun appendProvider(declaration: Declaration, scope: Scope): Analysis<Unit> {
                val provider = with(declaration) { Provider(id = id, node = node, dependencies = dependencies, scope = scope, declaration = this.declaration) }
                val previousProvider = providersIndex.put(provider.id, provider) ?: return SUCCESS
                val failureMessage = "Providers clash, a dependency type can only be provided once - typealias may help to break the clash"
                return fail(message = failureMessage, provider.declaration, previousProvider.declaration)
            }

            fun appendExporter(declaration: Declaration, delegated: Boolean): Analysis<Unit> {
                val exporterType = if (delegated) Exporter.Type.Delegated else Bracket(dependencies = declaration.dependencies)
                val exporter = with(declaration) { Exporter(id = id, node = node, type = exporterType, declaration = this.declaration) }
                val previousExporter = exportersIndex.put(exporter.id, exporter) ?: return SUCCESS
                return fail(message = "Exporters clash, an exporter can only be declared once", exporter.declaration, previousExporter.declaration)
            }

            return getDeclaredFunctions().onEachUntilError { method ->
                method.processStatement().flatMap { statement ->
                    when (statement) {
                        Statement.Irrelevant -> SUCCESS
                        is Declaration -> {
                            fun appendExported(scope: Scope) = appendProvider(statement, scope).flatMap { appendExporter(statement, true) }
                            when (statement.type) {
                                Declaration.Type.FACTORY -> appendProvider(statement, Scope.FACTORY)
                                Declaration.Type.SINGLE -> appendProvider(statement, Scope.SINGLE)
                                Declaration.Type.EAGER -> appendProvider(statement, Scope.EAGER)
                                Declaration.Type.EXPORT -> appendExporter(statement, false)
                                Declaration.Type.EXPORTED_FACTORY -> appendExported(Scope.FACTORY)
                                Declaration.Type.EXPORTED_SINGLE -> appendExported(Scope.SINGLE)
                                Declaration.Type.EXPORTED_EAGER -> appendExported(Scope.EAGER)
                            }
                        }
                    }
                }
            }
        }

        return graph.process().map { RawGraph(root = this, files = files, providersIndex = providersIndex, exporters = exportersIndex) }
    }
}

private fun validateExporters(exporters: Map<Id, Exporter>, providersIndex: Map<Id, Provider>): Analysis<Unit> {
    return exporters.values.asSequence().filter { exporter -> exporter.type is Bracket }.onEachUntilError { exporter ->
        if (exporter.id in providersIndex) {
            return@onEachUntilError fail<Unit>("Exporter declaration clash with provider", exporter.declaration, providersIndex.getValue(exporter.id).declaration)
        }
        val missingProviders = (exporter.type as Bracket).dependencies.asSequence().filter { dependency -> dependency.id !in providersIndex }.map(Dependency::declaration).toList()
        if (missingProviders.isNotEmpty()) {
            return@onEachUntilError fail<Unit>("Undefined provider for exporter's property", missingProviders.first(), *missingProviders.drop(1).toTypedArray())
        }
        SUCCESS
    }
}

private fun validateProvidersGraph(providers: Map<Id, Provider>): Analysis<Unit> {
    val validated = mutableSetOf<Id>()
    fun validateProvider(provider: Provider, path: Set<Id> = emptySet()): Analysis<Unit> {
        provider.dependencies.forEach { dep ->
            fun cycleList() = (path.toList() + provider.id + dep.id).joinToString(separator = " -> ") { dep -> dep.id.takeLastWhile { it != '.' } }
            val isValidated = dep.id in validated
            if (!isValidated && dep.id in path) return fail("Graph cycle detected ${cycleList()}", providers.getValue(dep.id).declaration)
            if (dep.isNullable && isValidated) return@forEach
            val depProvider = providers.getOrElse(dep.id) { return fail("The provider for dependency ${dep.name} is missing", provider.declaration) }
            if (!dep.isNullable && depProvider.isNullable) return fail("The provider for dependency ${dep.name} requires that ${dep.name} to be nullable", provider.declaration)
            if (!isValidated) validateProvider(depProvider, path + provider.id).getOr { failure -> return failure }
        }
        validated += provider.id
        return SUCCESS
    }
    providers.values.forEach { provider -> if (provider.id !in validated) validateProvider(provider).getOr { failure -> return failure } }
    return SUCCESS
}

private fun KSFunctionDeclaration.processStatement(): Analysis<Statement> {
    val returnType = checkNotNull(returnType?.resolve())

    fun getStatementType() = validateDeclaration().flatMap { checkNotNull(returnType.arguments.first().type).resolve().validateReturnType() }

    fun asProviderStatement(type: Declaration.Type): Analysis<Statement> {
        return getStatementType().flatMap { statementType ->
            parameters.asSequence().collect { param -> param.processProviderDependency() }.map { dependencies ->
                Declaration(id = statementType.id, type = type, node = statementType, dependencies = dependencies, declaration = this)
            }
        }
    }

    fun asExporterStatement(): Analysis<Statement> {
        if (!validateEmptyParameters()) return fail("Export declaration does not accept parameters", this)
        return getStatementType().flatMap { statementType ->
            statementType.declaration.validateExportDeclaration().flatMap { export ->
                export.getDeclaredProperties().collect { property -> property.processExporterDependency() }.map { dependencies ->
                    Declaration(id = statementType.id, type = Declaration.Type.EXPORT, node = statementType, dependencies = dependencies, declaration = this)
                }
            }
        }
    }

    if (returnType.isMarkedNullable) return Statement.IRRELEVANT
    return when (returnType.declaration.qualifiedName?.asString()) {
        FACTORY -> asProviderStatement(type = Declaration.Type.FACTORY)
        SINGLE -> asProviderStatement(type = Declaration.Type.SINGLE)
        EAGER -> asProviderStatement(type = Declaration.Type.EAGER)
        EXPORTED_FACTORY -> asProviderStatement(type = Declaration.Type.EXPORTED_FACTORY)
        EXPORTED_SINGLE -> asProviderStatement(type = Declaration.Type.EXPORTED_SINGLE)
        EXPORTED_EAGER -> asProviderStatement(type = Declaration.Type.EXPORTED_EAGER)
        EXPORT -> asExporterStatement()
        else -> Statement.IRRELEVANT
    }
}

private fun KSValueParameter.processProviderDependency(): Analysis<Dependency> {
    return validateProviderDependency().map { dep ->
        with(receiver = dep.type.resolve()) { Dependency(id = id, node = this, name = checkNotNull(name).asString(), isNullable = isMarkedNullable, declaration = dep) }
    }
}

private fun KSPropertyDeclaration.processExporterDependency(): Analysis<Dependency> {
    return validateExportDependency().map { dep ->
        with(receiver = dep.type.resolve()) { Dependency(id = id, node = this, name = simpleName.asString(), isNullable = isMarkedNullable, declaration = dep) }
    }
}

private val KSType.id get() = Id(id = (if (isMarkedNullable) makeNotNullable() else this).toTypeName().toString())

private fun KSClassDeclaration.validateGraphDeclaration(): Analysis<KSClassDeclaration> {
    return validateDeclarationClass().flatMap {
        if (!validateClassType()) return@flatMap fail("Only classes can be annotated as Graphs", this)
        success
    }
}

private fun KSDeclaration.validateExportDeclaration(): Analysis<KSClassDeclaration> {
    if (this !is KSClassDeclaration || !validateInterfaceType()) return fail("Only interfaces can be exported", this)
    return validateDeclarationClass().flatMap {
        val declaredFunctions = getDeclaredFunctions()
        if (!declaredFunctions.none()) {
            return@flatMap with(declaredFunctions.toList()) { fail("Only immutable properties are allowed for an exported type", first(), *drop(1).toTypedArray()) }
        }
        if (!validateSealedCondition()) return@flatMap fail("Sealed modifier is not allowed", this)
        success
    }
}

private fun KSClassDeclaration.validateDeclarationClass(): Analysis<KSClassDeclaration> {
    if (!validateInheritance()) return fail("Extend is not accepted for this declaration", this)
    if (!validateVisibility()) return fail("Only public or internal visibility modifiers are allowed", this)
    if (!validateGenerics()) return fail("This declaration cannot be parametrized with generic types", this)
    return success
}

private fun KSFunctionDeclaration.validateDeclaration(): Analysis<KSFunctionDeclaration> {
    if (!validateExtension()) return fail("Extensions are not allowed on kInject declarations", this)
    if (!validateVisibility()) return fail("Only public or internal visibility modifiers are allowed for kInject declarations", this)
    if (!validateSuspend()) return fail("Suspend functions are not allowed on kInject declarations", this)
    if (!validateGenerics()) return fail("A kInject declaration cannot be parametrized with generic types", this)
    return success
}

private fun KSType.validateReturnType(): Analysis<KSType> {
    if (declaration is KSTypeAlias && !declaration.validateVisibility()) return fail("Only public or internal visibility modifiers are allowed for typealias", declaration)
    return success
}

private fun KSValueParameter.validateProviderDependency(): Analysis<KSValueParameter> {
    if (!validateDefault()) return fail("Default values are not allowed", this)
    if (!validateVararg()) return fail("Vararg param is not allowed", this)
    return success
}

private fun KSPropertyDeclaration.validateExportDependency(): Analysis<KSPropertyDeclaration> {
    if (!validateMutability()) return fail("Only immutable properties are allowed for an exported type", this)
    if (!validateExtension()) return fail("Extension is not allowed for exported property", this)
    return success
}

private class RawGraph(val root: KSClassDeclaration, val files: List<KSFile>, val providersIndex: Map<Id, Provider>, val exporters: Map<Id, Exporter>)

private sealed interface Statement {
    companion object {
        val IRRELEVANT = Irrelevant.success
    }

    object Irrelevant : Statement
    class Declaration(val id: Id, val type: Type, val node: KSType, val dependencies: List<Dependency>, val declaration: KSFunctionDeclaration) : Statement {
        enum class Type { FACTORY, SINGLE, EAGER, EXPORT, EXPORTED_FACTORY, EXPORTED_SINGLE, EXPORTED_EAGER }
    }
}
