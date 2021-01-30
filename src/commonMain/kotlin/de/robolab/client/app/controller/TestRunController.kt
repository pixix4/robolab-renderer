package de.robolab.client.app.controller

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observeConst

class TestRunController(
    val traversalController: TestTraversalController
) {
    val title: ObservableValue<String> = "Run #${'$'}number ".observeConst()
}