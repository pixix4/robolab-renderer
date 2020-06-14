package de.robolab.common.planet

enum class PlanetVersion(val version: Int): IPlanetValue {
    /**
     * Planet file support is unknown
     */
    UNKNOWN(0),
    /**
     * Initial planet file version
     */
    V2018_SPRING(1),
    /**
     * Improved blocked path spline handling
     */
    V2020_SPRING(2),
    /**
     * Improved blocked path spline handling
     */
    V2020_FALL(3);

    override fun toString(): String {
        return "$name($version)"
    }

    companion object {
        val FALLBACK = V2018_SPRING

        val CURRENT = V2020_FALL

        fun parse(version: Int): PlanetVersion {
            return values().find { it.version == version } ?: FALLBACK
        }
    }
}
