package com.playground.metrics.service.service

import com.playground.metrics.service.model.Movie
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

@Service
class MovieService {
    private val log = LoggerFactory.getLogger(MovieService::class.java)

    private val movies =
        listOf(
            Movie(1, "The Shawshank Redemption", "Frank Darabont", 1994, "Drama", 9.3),
            Movie(2, "The Godfather", "Francis Ford Coppola", 1972, "Crime", 9.2),
            Movie(3, "The Dark Knight", "Christopher Nolan", 2008, "Action", 9.0),
            Movie(4, "Pulp Fiction", "Quentin Tarantino", 1994, "Crime", 8.9),
            Movie(5, "Inception", "Christopher Nolan", 2010, "Sci-Fi", 8.8),
            Movie(6, "Fight Club", "David Fincher", 1999, "Drama", 8.8),
            Movie(7, "The Matrix", "Lana Wachowski", 1999, "Sci-Fi", 8.7),
            Movie(8, "Interstellar", "Christopher Nolan", 2014, "Sci-Fi", 8.6),
            Movie(9, "Parasite", "Bong Joon-ho", 2019, "Thriller", 8.5),
            Movie(10, "Whiplash", "Damien Chazelle", 2014, "Drama", 8.5),
        )

    fun getAllMovies(): List<Movie> {
        simulateLatency()
        maybeFail()
        return movies
    }

    fun getMovieById(id: Long): Movie {
        simulateLatency()
        maybeFail()
        return movies.find { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie with id $id not found")
    }

    /**
     * Simulates realistic network/processing latency between 50ms and 500ms.
     */
    private fun simulateLatency() {
        val delay = Random.nextLong(50, 500)
        log.debug("Simulating {}ms latency", delay)
        Thread.sleep(delay)
    }

    /**
     * Simulates ~10% error rate to generate error metrics.
     */
    private fun maybeFail() {
        if (Random.nextInt(100) < 10) {
            log.warn("Simulating internal server error")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated service failure")
        }
    }
}
