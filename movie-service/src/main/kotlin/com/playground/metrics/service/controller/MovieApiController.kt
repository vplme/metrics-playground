package com.playground.metrics.service.controller

import com.playground.metrics.service.model.Movie
import com.playground.metrics.service.service.MovieService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
