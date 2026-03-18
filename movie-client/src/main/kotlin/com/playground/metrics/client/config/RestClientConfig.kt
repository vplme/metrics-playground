package com.playground.metrics.client.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun movieServiceRestClient(
        builder: RestClient.Builder,
        @Value("\${movie-service.base-url}") baseUrl: String,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .build()
}
