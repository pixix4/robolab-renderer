package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PlanetSignalGroup : IPlanetValue<PlanetSignalGroup> {

    abstract val flags: List<PlanetFlagSetter>
    abstract val tasks: List<PlanetSubscribableRef>
    abstract val triggers: List<PlanetSubscribableRef>

    @Serializable
    @SerialName("ORDERED")
    data class Ordered(
        val order: Long,
        override val flags: List<PlanetFlagSetter>,
        override val tasks: List<PlanetSubscribableRef>,
        override val triggers: List<PlanetSubscribableRef>,
    ) : PlanetSignalGroup() {

        override fun translate(delta: PlanetPoint) = copy(
            flags = flags.translate(delta),
            tasks = tasks.translate(delta),
            triggers = triggers.translate(delta),
        )

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
            flags = flags.rotate(direction, origin),
            tasks = tasks.rotate(direction, origin),
            triggers = triggers.rotate(direction, origin),
        )
    }

    @Serializable
    @SerialName("UNORDERED")
    data class Unordered(
        val label: String,
        override val flags: List<PlanetFlagSetter>,
        override val tasks: List<PlanetSubscribableRef>,
        override val triggers: List<PlanetSubscribableRef>,
    ) : PlanetSignalGroup() {

        override fun translate(delta: PlanetPoint) = copy(
            flags = flags.translate(delta),
            tasks = tasks.translate(delta),
            triggers = triggers.translate(delta),
        )

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
            flags = flags.rotate(direction, origin),
            tasks = tasks.rotate(direction, origin),
            triggers = triggers.rotate(direction, origin),
        )
    }
}
