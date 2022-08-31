package com.wokdsem.kinject2.app

import com.wokdsem.kinject2.Graph
import com.wokdsem.kinject2.export.export
import com.wokdsem.kinject2.scope.*

@Graph
class ApplicationGraph {
    fun provideMoviesAdviser(service: MoviesService) = single { MoviesAdviser(service) }
    fun provideMoviesService() = factory<MoviesService> { LocalMoviesService() }
    fun exportMovies() = export<Movies>()
}

fun main() {
    val graph = KApplicationGraph.from(graph = ApplicationGraph())
    println(message = graph.movies.adviser.recommendAMovie())
}

interface Movies {
    val adviser: MoviesAdviser
}

class MoviesAdviser(private val service: MoviesService) {
    fun recommendAMovie() = "You'll have a lot of fun watching ${service.getMovies().shuffled().first()}"
}

interface MoviesService {
    fun getMovies(): List<String>
}

class LocalMoviesService : MoviesService {
    override fun getMovies(): List<String> = listOf("Interestelar", "The Martian")
}
