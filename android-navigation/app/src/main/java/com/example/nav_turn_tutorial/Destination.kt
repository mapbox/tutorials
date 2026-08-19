package com.example.nav_turn_tutorial

import com.mapbox.geojson.Point

data class Destination(
    val name: String,
    val point: Point
)

val destinations = listOf(
    Destination("Statue of Liberty", Point.fromLngLat(-74.0445, 40.6892)),
    Destination("Empire State Building", Point.fromLngLat(-73.9857, 40.7484)),
    Destination("Central Park", Point.fromLngLat(-73.9654, 40.7829))
)
