package com.playground.metrics.service.model

data class Movie(
    val id: Long,
    val title: String,
    val director: String,
    val year: Int,
    val genre: String,
    val rating: Double,
)
