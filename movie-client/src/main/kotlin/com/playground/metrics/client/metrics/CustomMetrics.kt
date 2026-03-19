package com.playground.metrics.client.metrics

import com.playground.metrics.client.model.Movie
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

/**
 * Custom application metrics for tracking movie API interactions.
 *
 * These supplement the auto-instrumented HTTP client/server metrics
 * with business-level observability.
 */
@Component
class CustomMetrics(
    private val registry: MeterRegistry,
) {
    val moviesFetchedCounter: Counter =
        Counter
            .builder("movies.fetched")
            .description("Total number of movie fetch operations")
            .register(registry)

    val moviesFetchErrorCounter: Counter =
        Counter
            .builder("movies.fetch.errors")
            .description("Total number of failed movie fetch operations")
            .register(registry)

    val moviesFetchTimer: Timer =
        Timer
            .builder("movies.fetch.duration")
            .description("Duration of movie fetch operations")
            .publishPercentileHistogram()
            .register(registry)

    fun recordBestMatch(
        titleProvided: Boolean,
        yearProvided: Boolean,
        genreProvided: Boolean,
        movie: Movie?,
    ) {
        val found = movie != null
        Counter
            .builder("movie.bestmatch")
            .description("Best-match search requests with request/response field tracking")
            .tag("title_provided", titleProvided.toString())
            .tag("year_provided", yearProvided.toString())
            .tag("genre_provided", genreProvided.toString())
            .tag("found", found.toString())
            .tag("title_filled", (movie?.title?.isNotBlank() == true).toString())
            .tag("year_filled", (movie?.year?.let { it > 0 } == true).toString())
            .tag("genre_filled", (movie?.genre?.isNotBlank() == true).toString())
            .register(registry)
            .increment()
    }
}
