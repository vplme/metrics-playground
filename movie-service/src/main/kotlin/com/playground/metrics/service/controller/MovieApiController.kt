package com.playground.metrics.service.controller

import com.playground.metrics.service.model.Movie
import com.playground.metrics.service.service.MovieService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/movies")
class MovieApiController(
    private val movieService: MovieService,
) {
    @GetMapping
    fun getAllMovies(): List<Movie> = movieService.getAllMovies()

    @GetMapping("/{id}")
    fun getMovieById(
        @PathVariable id: Long,
    ): Movie = movieService.getMovieById(id)

    @GetMapping("/best-match")
    fun findBestMatch(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) genre: String?,
    ): Movie =
        movieService.findBestMatch(title, year, genre)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No matching movie found")
}
