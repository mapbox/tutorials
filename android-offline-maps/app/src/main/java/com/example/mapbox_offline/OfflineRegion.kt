package com.example.mapbox_offline

import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CoordinateBounds

data class OfflineRegion(
    val id: String,
    val name: String,
    val bounds: CoordinateBounds
) {
    // convert CoordinateBounds to Polygon (closed rectangular ring)
    val polygon: Polygon
        get() {
            val sw = bounds.southwest
            val ne = bounds.northeast
            val coords = listOf(
                Point.fromLngLat(sw.longitude(), sw.latitude()),
                Point.fromLngLat(ne.longitude(), sw.latitude()),
                Point.fromLngLat(ne.longitude(), ne.latitude()),
                Point.fromLngLat(sw.longitude(), ne.latitude()),
                Point.fromLngLat(sw.longitude(), sw.latitude())
            )
            return Polygon.fromLngLats(listOf(coords))
        }
}
