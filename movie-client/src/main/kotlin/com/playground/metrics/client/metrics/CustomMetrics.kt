package com.playground.metrics.client.metrics

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
    registry: MeterRegistry,
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
}
