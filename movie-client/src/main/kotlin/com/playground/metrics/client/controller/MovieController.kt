package com.playground.metrics.client.controller

import com.playground.metrics.client.client.MovieApiClient
import com.playground.metrics.client.metrics.CustomMetrics
import com.playground.metrics.client.model.Movie
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/movies")
class MovieController(
    private val movieApiClient: MovieApiClient,
    private val metrics: CustomMetrics,
) {
    private val log = LoggerFactory.getLogger(MovieController::class.java)

    @GetMapping
    fun getAllMovies(): List<Movie> =
        metrics.moviesFetchTimer.record<List<Movie>> {
            try {
                val movies = movieApiClient.getAllMovies()
                metrics.moviesFetchedCounter.increment()
                log.info("Fetched {} movies", movies.size)
                movies
            } catch (e: Exception) {
                metrics.moviesFetchErrorCounter.increment()
                log.error("Failed to fetch movies", e)
                throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Movie service unavailable", e)
            }
        }!!

    @GetMapping("/{id}")
    fun getMovieById(
        @PathVariable id: Long,
    ): Movie =
        metrics.moviesFetchTimer.record<Movie> {
            try {
                val movie =
                    movieApiClient.getMovieById(id)
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found")
                metrics.moviesFetchedCounter.increment()
                log.info("Fetched movie: {}", movie.title)
                movie
            } catch (e: ResponseStatusException) {
                metrics.moviesFetchErrorCounter.increment()
                throw e
            } catch (e: Exception) {
                metrics.moviesFetchErrorCounter.increment()
                log.error("Failed to fetch movie {}", id, e)
                throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Movie service unavailable", e)
            }
        }!!

    @GetMapping("/best-match")
    fun findBestMatch(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) genre: String?,
    ): Movie {
        val movie =
            try {
                movieApiClient.findBestMatch(title, year, genre)
            } catch (e: Exception) {
                log.error("Failed to find best match: title={}, year={}, genre={}", title, year, genre, e)
                metrics.recordBestMatch(
                    titleProvided = !title.isNullOrBlank(),
                    yearProvided = year != null,
                    genreProvided = !genre.isNullOrBlank(),
                    movie = null,
                )
                throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Movie service unavailable", e)
            }

        metrics.recordBestMatch(
            titleProvided = !title.isNullOrBlank(),
            yearProvided = year != null,
            genreProvided = !genre.isNullOrBlank(),
            movie = movie,
        )

        if (movie != null) {
            log.info("Found best match: {}", movie.title)
            return movie
        }

        throw ResponseStatusException(HttpStatus.NOT_FOUND, "No matching movie found")
    }
}
