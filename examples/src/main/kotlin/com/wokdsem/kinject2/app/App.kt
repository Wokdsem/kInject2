package com.wokdsem.kinject2.app

import com.wokdsem.kinject2.Graph
import com.wokdsem.kinject2.scope.factory

@Graph
class ApplicationGraph {
    fun provideMoviesAdviser(service: MoviesService) = factory { MoviesAdviser(service) }
    fun provideMoviesService() = factory<MoviesService> { LocalMoviesService() }
}

fun main() {
    KApplicationGraph.from(graph = ApplicationGraph())
}


class MoviesAdviser(private val service: MoviesService) {
    fun recommendAMovie() = "You'll have a lot of fun if you watch ${service.getMovies().shuffled().first()}"
}

interface MoviesService {
    fun getMovies(): List<String>
}

class LocalMoviesService : MoviesService {
    override fun getMovies(): List<String> = listOf("Interestelar", "The Martian")
}
