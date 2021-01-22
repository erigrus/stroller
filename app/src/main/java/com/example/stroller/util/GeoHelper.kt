package com.example.stroller.util

import net.sf.geographiclib.Geodesic

/**
 * Holds latitude and longitude in degrees of a geographic point.
 */
class GeoPoint(lat: Double, lon: Double)
{
    val lat = lat
    val lon = lon
}

/**
 * Calculates the distance in meters between two GeoPoints.
 */
fun distance(pointA: GeoPoint, pointB: GeoPoint): Double
{
    val g = Geodesic.WGS84.Inverse(pointA.lat, pointA.lon, pointB.lat, pointB.lon)
    return g.s12
}

/**
 * Translates a GeoPoint to a new position with an absolute direction in degrees
 * and a distance in meters.
 */
fun translate(point: GeoPoint, distance: Double, direction: Double): GeoPoint
{
    val g = Geodesic.WGS84.Direct(point.lat, point.lon, direction, distance)
    return GeoPoint(g.lat2, g.lon2)
}

/**
 * Checks if two lines intersect.
 */
fun intersect(lineA: List<GeoPoint>, lineB: List<GeoPoint>): Boolean
{
    // lines cannot intersect because at least one of them is singular
    if (lineA.size < 2 || lineB.size < 2)
    {
        return false
    }

    var listIterator = lineB.listIterator()
    var intersects = false

    // move the pointer to the first element
    listIterator.next()

    while (listIterator.hasNext())
    {
        // get next element and previous as a line segment
        val pointA = listIterator.next()
        val pointB = lineB[listIterator.previousIndex()]

        // check intersection
        intersects = intersect(pointA, pointB, lineA[0], lineA[1])

        if (intersects)
            return intersects
    }

    return intersect(lineA.subList(1, lineA.size - 1), lineB)
}

/**
 * Checks if two line segments intersect. Line A is defined by AB, line B by DC
 */
fun intersect(A: GeoPoint, B: GeoPoint, C: GeoPoint, D: GeoPoint): Boolean
{
    return ccw(A,C,D) != ccw(B,C,D) and ccw(A,B,C) != ccw(A,B,D)
}

fun ccw(A: GeoPoint, B: GeoPoint, C: GeoPoint): Boolean
{
    return (C.lon-A.lon)*(B.lat-A.lat) > (B.lon-A.lon)*(C.lat-A.lat)
}

