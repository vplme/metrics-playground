package com.playground.metrics.client.client

import com.playground.metrics.client.model.Movie
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MovieApiClient(
    private val movieServiceRestClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(MovieApiClient::class.java)

    fun getAllMovies(): List<Movie> {
        log.debug("Fetching all movies from movie-service")
        return movieServiceRestClient
            .get()
            .uri("/api/movies")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<Movie>>() {})
            ?: emptyList()
    }

    fun getMovieById(id: Long): Movie? {
        log.debug("Fetching movie {} from movie-service", id)
        return movieServiceRestClient
            .get()
            .uri("/api/movies/{id}", id)
            .retrieve()
            .body(Movie::class.java)
    }

    fun findBestMatch(title: String?, year: Int?, genre: String?): Movie? {
        log.debug("Finding best match: title={}, year={}, genre={}", title, year, genre)
        return movieServiceRestClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/movies/best-match")
                    .apply {
                        title?.let { queryParam("title", it) }
                        year?.let { queryParam("year", it) }
                        genre?.let { queryParam("genre", it) }
                    }
                    .build()
            }
            .retrieve()
            .body(Movie::class.java)
    }
}
