package de.robolab.common.planet.utils

object PlanetVersion {

    /**
     * Planet file support is unknown
     */
    const val UNKNOWN: Long = 0L

    /**
     * Initial planet file version
     */
    const val V2018_SPRING: Long = 1L

    /**
     * Improved blocked path spline handling
     */
    const val V2020_SPRING: Long = 2L

    /**
     * Improved blocked path spline handling
     */
    const val V2020_FALL: Long = 3L

    /**
     * Improved blocked path spline handling
     */
    const val V2021_FALL: Long = 4L

    const val FALLBACK = V2018_SPRING

    const val CURRENT = V2021_FALL
}
