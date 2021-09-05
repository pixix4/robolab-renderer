package de.robolab.common.testing

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.test.PlanetSubscribableRef
import de.robolab.common.planet.utils.LookupPlanet

data class TestFlagSetter(
    val subscribable: PlanetSubscribableRef,
    val signal: TestSignal?,
    val type: Type,
    val value: Boolean
) {
    constructor(subscribable: PlanetSubscribableRef, signal: TestSignal?, type: Pair<Type, Boolean>) : this(
        subscribable,
        signal,
        type.first,
        type.second
    )

    fun translate(delta: PlanetPoint): TestFlagSetter =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestFlagSetter =
        copy(subscribable = subscribable.rotate(direction, origin))

    enum class Type(
        val negatedName: String,
        val creator: (
            activate: Set<TestSignal>,
            deactivate: Set<TestSignal>,
            PlanetSubscribableRef,
            defaultActive: Boolean
        ) -> TestSignalFlag
    ) {
        DISALLOW("ALLOW", TestSignalFlag::Disallow) {
            override fun getDefault(subscribable: PlanetSubscribableRef, planet: LookupPlanet): Boolean =
                when (subscribable) {
                    is PlanetSubscribableRef.Path -> planet.getPath(
                        PlanetPoint(subscribable.x, subscribable.y),
                        subscribable.direction
                    )?.hidden ?: throw NoSuchElementException("Could not find referenced path @$subscribable")
                    is PlanetSubscribableRef.Node -> planet.getPaths(subscribable.point).all(PlanetPath::hidden)
                }
        },
        SKIP("UNSKIP", TestSignalFlag::Skip) {
            override fun getDefault(subscribable: PlanetSubscribableRef, planet: LookupPlanet): Boolean = false
        };

        fun nameWithValue(value: Boolean): String = (if (value) name else negatedName).lowercase()

        abstract fun getDefault(subscribable: PlanetSubscribableRef, planet: LookupPlanet): Boolean

        companion object {
            fun fromString(name: String): Pair<Type, Boolean> {
                val canonicalName = name.trim().uppercase()
                return values().mapNotNull {
                    when (canonicalName) {
                        it.name -> it to true
                        it.negatedName -> it to false
                        else -> null
                    }
                }.first()
            }
        }
    }
}

fun TestFlagSetter.serializeType(): String {
    return type.nameWithValue(value)
}
