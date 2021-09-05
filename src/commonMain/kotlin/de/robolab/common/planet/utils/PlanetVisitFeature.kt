package de.robolab.common.planet.utils

import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.PlanetTarget

data class PlanetVisitFeature(val revealedPaths: List<PlanetPath>, val setTargets: List<PlanetTarget>)