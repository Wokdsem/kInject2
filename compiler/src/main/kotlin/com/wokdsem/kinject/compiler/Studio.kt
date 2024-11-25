package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Visibility.PUBLIC
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.wokdsem.kinject.compiler.Export.Type.*
import com.wokdsem.kinject.compiler.Statement.*

internal fun processGraphDeclaration(graphDeclaration: KSClassDeclaration): Analysis<Graph> {
    return graphDeclaration.collectGraph()
        .validate { rawGraph -> validateExporters(rawGraph.exporters, rawGraph.providersIndex) }
        .validate { rawGraph -> validateProvidersGraph(rawGraph.providersIndex, rawGraph.claimedProviders) }.map { rawGraph ->
            with(rawGraph) { Graph(root = root, files = files, modules = modules, providers = providersIndex, exports = exporters.values.toList()) }
        }
}

private fun KSClassDeclaration.collectGraph(): Analysis<RawGraph> {
    class ModuleDeclaration(val providers: List<Id>, val imports: List<Id>)
    return validateGraphDeclaration().flatMap { graph ->
        val files = mutableSetOf<KSFile>()
        val modulesIndex = mutableMapOf<Id, Module>()
        val providersIndex = mutableMapOf<Id, Provider>()
        val exportersIndex = mutableMapOf<Id, Export>()
        val claimedProviders = mutableSetOf<Id>()

        fun KSClassDeclaration.process(modulesPath: Map<Id, KSFunctionDeclaration> = emptyMap()): Analysis<ModuleDeclaration> {

            fun appendModule(import: Import): Analysis<Unit> {
                fun fail(node: KSNode) = fail<Unit>("Modules clash, a module can be imported only once", node, import.declaration)
                val moduleId = import.node.id
                return when (moduleId) {
                    id -> fail(this)
                    in modulesPath -> fail(node = modulesPath.getValue(moduleId))
                    in modulesIndex -> fail(node = modulesIndex.getValue(moduleId).declaration)
                    else -> import.nodeDeclaration.process(modulesPath = modulesPath + (moduleId to import.declaration)).validate { declaration ->
                        when {
                            declaration.providers.isNotEmpty() || declaration.imports.isNotEmpty() -> SUCCESS
                            else -> fail("A module must at least provide one dependency or import another module", import.declaration)
                        }.onSuccess { modulesIndex[moduleId] = Module(moduleId, import.node, declaration.providers, declaration.imports, id, import.declaration) }
                    }
                }.map { }
            }

            fun appendProvider(declaration: Declaration, scope: Scope, exported: Boolean): Analysis<Unit> {
                val providerId = declaration.node.id
                return when (providerId) {
                    !in providersIndex -> SUCCESS
                    else -> {
                        val failureMessage = "Providers clash, a dependency type can only be provided once - typealias may help resolve the clash"
                        fail(message = failureMessage, declaration.declaration, providersIndex.getValue(providerId).declaration)
                    }
                }.onSuccess { providersIndex[providerId] = with(declaration) { Provider(providerId, scope, exported, node, dependencies, id, this.declaration) } }
            }

            fun appendExporter(declaration: Declaration, delegated: Boolean): Analysis<Unit> {
                val exporterId = declaration.node.id
                return when (exporterId) {
                    !in exportersIndex -> SUCCESS
                    else -> fail(message = "Exporters clash, an exporter can only be declared once", declaration.declaration, exportersIndex.getValue(exporterId).declaration)
                }.onSuccess {
                    val exporterType = if (delegated) Delegated else Bracket(dependencies = declaration.dependencies)
                    exportersIndex[exporterId] = with(declaration) { Export(exporterId, node, exporterType, this.declaration) }
                }
            }

            containingFile?.let { file -> files += file }
            val providers = mutableListOf<Id>()
            val imports = mutableListOf<Id>()
            val requirePublicVisibility = getVisibility() == PUBLIC
            return getDeclaredFunctions().filter { declaration -> declaration.getVisibility() == PUBLIC }.onEachUntilError { method ->
                method.processStatement(requirePublicVisibility).flatMap { statement ->
                    when (statement) {
                        Irrelevant -> SUCCESS
                        is Import -> appendModule(statement).onSuccess { imports += statement.node.id }
                        is Declaration -> {
                            fun appendExport() = appendExporter(statement, false).onSuccess { claimedProviders += statement.dependencies.map(Dependency::id) }
                            fun appendProvider(scope: Scope, exported: Boolean = false): Analysis<Unit> {
                                return appendProvider(statement, scope, exported)
                                    .onSuccess {
                                        providers += statement.node.id
                                        claimedProviders += statement.dependencies.map(Dependency::id)
                                    }
                            }

                            fun appendExported(scope: Scope): Analysis<Unit> {
                                return appendProvider(scope, true).flatMap { appendExporter(statement, true) }.onSuccess { claimedProviders += statement.node.id }
                            }
                            when (statement.type) {
                                Declaration.Type.FACTORY -> appendProvider(Scope.FACTORY)
                                Declaration.Type.SINGLE -> appendProvider(Scope.SINGLE)
                                Declaration.Type.EAGER -> appendProvider(Scope.EAGER)
                                Declaration.Type.EXPORT -> appendExport()
                                Declaration.Type.EXPORTED_FACTORY -> appendExported(Scope.FACTORY)
                                Declaration.Type.EXPORTED_SINGLE -> appendExported(Scope.SINGLE)
                                Declaration.Type.EXPORTED_EAGER -> appendExported(Scope.EAGER)
                            }
                        }
                    }
                }
            }.map { ModuleDeclaration(providers = providers, imports = imports) }
        }

        return graph.process().map {
            RawGraph(
                root = this,
                files = files.toList(),
                modules = modulesIndex.values.toList(),
                providersIndex = providersIndex,
                exporters = exportersIndex,
                claimedProviders = claimedProviders
            )
        }
    }
}

private fun validateExporters(exporters: Map<Id, Export>, providersIndex: Map<Id, Provider>): Analysis<Unit> {
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

private fun validateProvidersGraph(providers: Map<Id, Provider>, directDependencies: Set<Id>): Analysis<Unit> {
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
        if ((provider.scope == Scope.SINGLE || provider.scope == Scope.FACTORY) && provider.id !in directDependencies) {
            return fail("Dead/unused provider declaration", provider.declaration)
        }
        validated += provider.id
        return SUCCESS
    }
    providers.values.forEach { provider -> if (provider.id !in validated) validateProvider(provider).getOr { failure -> return failure } }
    return SUCCESS
}

private fun KSFunctionDeclaration.processStatement(publicVisibilityRequired: Boolean): Analysis<Statement> {
    val functionReturnType = checkNotNull(returnType?.settle())

    fun getStatementType(): Analysis<KSType> {
        return validateDeclaration().flatMap {
            functionReturnType.flatMap { type ->
                checkNotNull(type.arguments.first().type).settle().flatMap { argumentType ->
                    argumentType.validateReturnType(this, publicVisibilityRequired)
                }
            }
        }
    }

    fun asImportStatement(): Analysis<Statement> {
        if (!validateEmptyParameters()) return fail("Import declaration does not accept parameters", this)
        return getStatementType().flatMap { statementType ->
            statementType.declaration.validateImportDeclaration().map { import ->
                Import(node = statementType, nodeDeclaration = import, declaration = this)
            }
        }
    }

    fun asProviderStatement(type: Declaration.Type): Analysis<Statement> {
        return getStatementType().flatMap { statementType ->
            parameters.asSequence().collect { param -> param.processProviderDependency() }.map { dependencies ->
                Declaration(type = type, node = statementType, dependencies = dependencies, declaration = this)
            }
        }
    }

    fun asExporterStatement(): Analysis<Statement> {
        if (!validateEmptyParameters()) return fail("Export declaration does not accept parameters", this)
        return getStatementType().flatMap { statementType ->
            statementType.declaration.validateExportDeclaration().flatMap { export ->
                export.getDeclaredProperties().collect { property -> property.processExporterDependency() }.map { dependencies ->
                    Declaration(type = Declaration.Type.EXPORT, node = statementType, dependencies = dependencies, declaration = this)
                }
            }
        }
    }

    return functionReturnType.flatMap { type ->
        if (type.isMarkedNullable) return Statement.IRRELEVANT
        when (type.declaration.qualifiedName?.asString()) {
            IMPORT -> asImportStatement()
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
}

private fun KSValueParameter.processProviderDependency(): Analysis<Dependency> {
    return validateProviderDependency().flatMap { dep ->
        dep.type.settle().map { dependencyType ->
            dependencyType.run {
                Dependency(id = id, node = this, name = checkNotNull(name).asString(), isNullable = isMarkedNullable, declaration = dep)
            }
        }
    }
}

private fun KSPropertyDeclaration.processExporterDependency(): Analysis<Dependency> {
    return validateExportDependency().flatMap { dep ->
        dep.type.settle().map { dependencyType ->
            dependencyType.run {
                Dependency(id = id, node = this, name = simpleName.asString(), isNullable = isMarkedNullable, declaration = dep)
            }
        }
    }
}

private val KSClassDeclaration.id get() = Id(id = toClassName().toString())
private val KSType.id get() = Id(id = (if (isMarkedNullable) makeNotNullable() else this).toTypeName().toString())

@OptIn(KspExperimental::class)
private fun KSClassDeclaration.validateGraphDeclaration(): Analysis<KSClassDeclaration> {
    return validateDeclarationClass().flatMap {
        if (!validateVisibility()) return@flatMap fail("Only public or internal visibility modifiers are allowed for graphs", this)
        if (!validateClassType()) return@flatMap fail("Only classes can be annotated as Graphs", this)
        if (!getAnnotationsByType(com.wokdsem.kinject.Graph::class).first().validateName()) return@flatMap fail("Invalid graph name, characters ` and \\ are not allowed", this)
        success
    }
}

private fun KSDeclaration.validateImportDeclaration(): Analysis<KSClassDeclaration> {
    if (this !is KSClassDeclaration) return fail("Only a variant of class declaration can be imported", this)
    return validateDeclarationClass()
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
    if (!validateGenerics()) return fail("This declaration cannot be parametrized with generic types", this)
    if (!validateInheritance()) return fail("Extend is not accepted for this declaration", this)
    return success
}

private fun KSFunctionDeclaration.validateDeclaration(): Analysis<KSFunctionDeclaration> {
    if (!validateExtension()) return fail("Extensions are not allowed on kInject declarations", this)
    if (!validateSuspend()) return fail("Suspend functions are not allowed on kInject declarations", this)
    if (!validateGenerics()) return fail("A kInject declaration cannot be parametrized with generic types", this)
    return success
}

private fun KSTypeReference.settle(): Analysis<KSType> {
    val type = resolve()
    return when {
        type.isError -> fail("Type cannot be resolved. For further details about this error, refer to the 'Well-known Issues' section in the readme.", this)
        else -> type.success
    }
}

private fun KSType.validateReturnType(
    fDeclaration: KSFunctionDeclaration, publicVisibilityRequired: Boolean
): Analysis<KSType> {
    if (declaration !is KSTypeAlias) return success
    fun fail(message: String) = fail<KSType>(message, fDeclaration, declaration)
    when {
        !publicVisibilityRequired -> if (!declaration.validateVisibility()) return fail("Only public or internal visibility modifiers are allowed for typealias")
        !declaration.validatePublicVisibility() -> return fail("Typealias visibility must not be more restrictive than the class where it's used")
    }
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

private class RawGraph(
    val root: KSClassDeclaration,
    val files: List<KSFile>,
    val modules: List<Module>,
    val providersIndex: Map<Id, Provider>,
    val exporters: Map<Id, Export>,
    val claimedProviders: Set<Id>
)

private sealed interface Statement {
    companion object {
        val IRRELEVANT = Irrelevant.success
    }

    data object Irrelevant : Statement
    class Import(val node: KSType, val nodeDeclaration: KSClassDeclaration, val declaration: KSFunctionDeclaration) : Statement
    class Declaration(val type: Type, val node: KSType, val dependencies: List<Dependency>, val declaration: KSFunctionDeclaration) : Statement {
        enum class Type { FACTORY, SINGLE, EAGER, EXPORT, EXPORTED_FACTORY, EXPORTED_SINGLE, EXPORTED_EAGER }
    }
}
