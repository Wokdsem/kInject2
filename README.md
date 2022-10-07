# *kInject2* (k2)

#### A kotlin multiplatform dependency injection framework powered by [KSP](https://github.com/google/ksp) & [KotlinPoet](https://github.com/square/kotlinpoet)

<sub><sup>*kInject2 is based on kInject, a dependency injection framework for Java. The initial *k* is not because this framework is Kotlin-oriented.</sup></sub>

---
In software, when you are modeling a solution, it's pretty likely that you discover yourself just defining a set of entities or components with different responsibilities
that interact with each other. When entity A interacts with entity B, we say that A has a dependency on B.
If we represent all these relationships in our system as a directed graph, we get a graph of dependency.

But things are not that simple, as our system evolves and grows, we quickly realize that these relationships of dependency cannot be established randomly, and soon concepts
such as Inversion of control, Inversion of dependency, and Dependency Injection come up.

## This is kInject2

k2 is a compile-time framework for dependency injection for Kotlin. It is designed to be easy-to-learn and easy-to-use.

### The dependency graph

The first step to use k2 is to build a dependency graph.

```kotlin
@Graph
class MyFirstGraph
```

We annotate a class with ```@Graph``` to let k2 know we want to use this class as a dependency graph.  
The example above is a valid graph, but it's an empty graph, not very useful so far.

### Declaring dependencies

A dependency graph isn't very useful if we don't have any dependencies, let's declare our first dependency.

```kotlin
@Graph
class MyFirstGraph {
    fun provideNumber() = single { 8 }
}
```

This declaration add a dependency with type ```Int``` to the graph.

#### Type inference

In the example above, the ```Int``` type is inferred automatically. However, it's possible you may need to override the default type inference, you can do it by using one of the
following ways:

```kotlin
fun provideNumber() = single<Number> { 8 }
```

```kotlin
fun provideNumber(): Single<Number> = single { 8 }
```

#### Scopes

A dependency is always declared along with its scope. The scope is applicable in the context of the graph where is declared, thus,
if the same dependency is declared in a different graph, the scope in one graph doesn't have any effect on the other.

* **Single** The single scope guarantees that only one instance of this dependency will be created in the graph instance.

```kotlin
fun provideSingleNumber() = single { 5 }
```

* **Eager** The eager scope is a variant of single, only one instance will be created in the graph and this will be initialized as soon as the graph is loaded.

```kotlin
fun provideSingleNumber() = eager { 5 }
```

* **Factory** A new instance will be returned every time a dependency with factory scope is injected.

```kotlin
fun provideSingleNumber() = factory { 5 }
```

#### Type alias

You can only have one provider declaration per type, but there may be times you need to add dependencies with the same type to the graph. For these cases,
you can use Typealias to distinguish them. Typealias is not resolved during the graph compilation, thus a typealias will be considered a different type than its underlying type.

```kotlin
typealias Password = String

@Graph
class MyFirstGraph {
    fun provideText() = single { "This is awesome" }
    fun providePassword() = single<Password> { "Secret password" }
}
```

#### Type erasure

Type erasure, not a problem for k2. If you declare dependencies with generics, k2 will be able to deal with them.

```kotlin
@Graph
class MyFirstGraph {
    fun provideListOfNumbers() = single { listOf(1, 2, 3) }
    fun provideListOfStrings() = single { listOf("1", "2", "3") }
}
```

#### The dependency's dependencies

So far, we've seen how to declare a dependency, but we haven't established any connection between them. To inject the dependencies of the dependency you are declaring,
simply add them as parameters of the function where the dependency is provided. k2 will throw a compile-time error if a dependency cycle is detected or if any of the
dependencies are unknown.

```kotlin
@Graph
class MyFirstGraph {
    fun provideNumber() = single { 8 }
    fun provideText(times: Int) = single { "*".repeat(times) }
}
```

#### Optional types

You can mark dependencies as optional in your declarations. The declared type will be added to the graph and can be used as usual. However, 
if a type that was declared as optional is set as dependency of a dependency, this must set the optionality too, otherwise, an
error will be thrown in compile-time. A non-optional declaration can resolve both, optional and non-optional dependencies. 

```kotlin
@Graph
class MyFirstGraph {
    fun provideNonOptionalText() = single { "Text" }
    fun provideOptionalNumber() = single<Int?> { null }
    fun provideTextAndNumber(text: String?, number: Int?) = single {
        // text will be supplied by the first declaration as a non-optional type can resolve optional
        // number will be supplied by the second declaration, compilation fails if number isn't marked as optional
        TextAndNumber(text = text, number = number)
    }
}
data class OptionalTextAndNumber(val text: String?, val number: Int?)
```

#### Function type

Sometimes, you may need to instantiate dependencies that have dependencies that are only known in runtime. For these cases, providing a function may help.

```kotlin
@Graph
class MyFirstGraph {
    fun provideSymbol() = single { '*' }
    fun provideText(symbol: Char) = single { times: Int -> symbol.toString().repeat(times) }
}
```

#### Modules

As a project grows, it's possible that your graph does too, and you may prefer to break your graph down into multiple chunks. In this context, these chunks are known as modules.   
A module is nothing but a container of dependencies that can be imported into a graph. A module can also import other modules.
All the dependencies declared in a module will be added to the dependency graph when imported.

```kotlin
class MyFirstGraph {
    fun importModule() = import { MyFirstModule() }
}

class MyFirstModule {
    fun provideText(times: Int) = single { "*".repeat(times) }
    fun importAnotherModule() = import { AnotherModule() }
}

class AnotherModule {
    fun provideNumber() = single { 8 }
}
```

### Building the graph

Once you have declared a graph and supplied all the dependencies that make up the graph, it's time to use it.  
The graph is analyzed in compile-time, and if all requirements are met, k2 will generate a K<Graph> class containing all processed dependencies.

```kotlin
@Graph
class MyFirstGraph {
    fun provideNumber() = single { 8 }
}

fun main() {
    val kGraph = KMyFirstGraph.from(graph = MyFirstGraph())
}
```

Congratulations, you got your graph. However, if you try to use it, you'll see there's nothing accessible there. There's still one last step.

#### Export

k2 requires that you make dependencies publicly accessible explicitly. For that, use the export declaration and set an interface that contains the types you need to export.   
You can add as many export declarations as you need. A compile-time error will be thrown if asking to export a dependency that is not part of the graph.

```kotlin
@Graph
class MyFirstGraph {

    fun provideNumber() = single { 8 }
    fun provideText(times: Int) = single { "*".repeat(times) }
    fun providePrinter() = single { Printer { message -> println(message) } }

    fun exportDeps() = export<Deps>()

}

interface Deps {
    val printer: Printer
    val text: String
}

fun main() {
    val kGraph = KMyFirstGraph.from(graph = MyFirstGraph())
    val deps = kGraph.deps
    deps.printer.print(deps.text) // prints ********
}
```

#### Export shortcut

k2 provides a variation of the scope declaration to export the type directly. These declarations are known as export shortcuts:
```exportSingle```, ```exportEager```, ```exportFactory```.

```kotlin
@Graph
class MyFirstGraph {
    fun providePrinter() = exportSingle { Printer { message -> println(message) } }
}

fun main() {
    val kGraph = KMyFirstGraph.from(graph = MyFirstGraph())
    kGraph.printer.print("This printer instance was exported through a shortcut")
}
```  

## Setup

#### KSP 

First, apply the KSP plugin in your project build.gradle:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}
```

#### Kotlin(JVM) - Android

For Kotlin(JVM) or Android targets, check both, kinject and compiler, are added as follows in your build.gradle:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wokdsem.kinject:kinject:2.0.1")
    ksp("com.wokdsem.kinject:compiler:2.0.1")
}
```

#### Multiplatform

For a multiplatform target, check both, kinject and compiler, are added as follows in your build.gradle:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("com.wokdsem.kinject:kinject:2.0.1")
    add("kspCommonMainMetadata", "com.wokdsem.kinject:compiler:2.0.1")
}

afterEvaluate {
    tasks {
        withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}
```

For further information about how to set up a KSP dependency for other compilation targets other than CommonMain, please follow
this [link](https://kotlinlang.org/docs/ksp-multiplatform.html).

#### Generated code

Last, add the following configuration so that your IDE is able to index the generated code.

```kotlin
// JVM/Android
kotlin {
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
}

// Multiplatform
kotlin {
    sourceSets.commonMain { kotlin.srcDir("build/generated/ksp/metadata/commonmain/kotlin") }
}
```

**Adjust configuration when targeting other platforms and/or source-sets configurations.*

## Incremental compilation

Incremental compilation is enabled by default. A graph compilation is reused as long as the graph definition remains stable.

## Why kInject2?

There is no intent to hide this is a very opinionated solution in terms of how software should be built and especially how a dependency injection framework should be used.
Needless to say, you can find a bunch of great DI solutions out there, if you are happy with yours, that's great, keep with it.  
However, if you agree with any of the following pain-points and/or bad smells that other solutions add to your code, I encourage you to keep reading and
give k2 a try as this solution is designed to avoid them.

* Your business logic classes shouldn't know anything about the dependency injector, that includes extending the framework classes or adding @Inject annotations or similar.
* A class shouldn't break the encapsulation to allow the injector to inject dependencies from the outside.
* Solutions based on reflection are hard to escalate and are error-prone.
* Kotlin is a powerful language, the injection framework should avail of this power, which includes Typealias, Functions, Generics, and so on.
* Writing tests to wrap the tool's weaknesses is a waste of time.

## Roadmap

#### 2.1.X

- Allow overriding the default <K> generated graph class name
- Allow overriding the default name of exported properties in the generated graph

## License

```
Copyright 2022 Wokdsem

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```