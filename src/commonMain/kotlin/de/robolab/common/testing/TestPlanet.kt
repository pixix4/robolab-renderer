package de.robolab.common.testing

import de.robolab.common.planet.LookupPlanet
import de.robolab.common.planet.Coordinate

class TestPlanet(
    val planet: LookupPlanet,
    val explorationGoals: Set<Coordinate?>,
    val targetGoals: Set<Coordinate?>,
    val tasks: List<TestTask>,
    val triggers: List<TestTrigger>,
    val flags: List<TestSignalFlag>,
    val signals: Map<TestSignal, TestSignalGroup>,
) {

}