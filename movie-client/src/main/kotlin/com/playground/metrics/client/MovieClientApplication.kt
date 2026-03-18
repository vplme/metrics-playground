package com.playground.metrics.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MovieClientApplication

fun main(args: Array<String>) {
    runApplication<MovieClientApplication>(*args)
}
