package com.wokdsem.kinject.compiler

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Visibility.INTERNAL
import com.google.devtools.ksp.symbol.Visibility.PUBLIC
import com.squareup.kotlinpoet.ksp.toTypeName
import com.wokdsem.kinject.Graph

internal fun KSDeclaration.validatePublicVisibility(): Boolean = getVisibility() == PUBLIC
internal fun KSDeclaration.validateVisibility(): Boolean = with(getVisibility()) { this == PUBLIC || this == INTERNAL }
internal fun KSDeclaration.validateGenerics(): Boolean = typeParameters.isEmpty()

internal fun KSClassDeclaration.validateClassType(): Boolean = classKind == ClassKind.CLASS
internal fun KSClassDeclaration.validateInterfaceType(): Boolean = classKind == ClassKind.INTERFACE
internal fun KSClassDeclaration.validateInheritance(): Boolean = superTypes.all { it.toTypeName().toString() == "kotlin.Any" }
internal fun KSClassDeclaration.validateSealedCondition(): Boolean = Modifier.SEALED !in modifiers

internal fun KSFunctionDeclaration.validateExtension(): Boolean = extensionReceiver == null
internal fun KSFunctionDeclaration.validateEmptyParameters(): Boolean = parameters.isEmpty()
internal fun KSFunctionDeclaration.validateSuspend(): Boolean = Modifier.SUSPEND !in modifiers
internal fun KSFunctionDeclaration.validateGenerics(): Boolean = typeParameters.isEmpty()

internal fun KSValueParameter.validateDefault(): Boolean = !hasDefault
internal fun KSValueParameter.validateVararg(): Boolean = !isVararg

internal fun KSPropertyDeclaration.validateExtension(): Boolean = extensionReceiver == null
internal fun KSPropertyDeclaration.validateMutability(): Boolean = !isMutable

internal fun Graph.validateName(): Boolean = name.all { char -> char != '`' && char != '\\' }
