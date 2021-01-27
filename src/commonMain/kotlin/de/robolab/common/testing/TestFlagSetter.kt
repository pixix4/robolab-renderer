package de.robolab.common.testing

import de.robolab.common.planet.LookupPlanet
import de.robolab.common.planet.Path
import de.robolab.common.planet.Planet
import de.robolab.common.planet.SubscribableIdentifier

data class TestFlagSetter(
    val subscribable: SubscribableIdentifier<*>,
    val signal: TestSignal?,
    val type: Type,
    val value: Boolean
) {
    constructor(subscribable: SubscribableIdentifier<*>, signal: TestSignal?, type: Pair<Type, Boolean>) : this(
        subscribable,
        signal,
        type.first,
        type.second
    )

    fun translate(delta: de.robolab.common.planet.Coordinate): TestFlagSetter =
        copy(subscribable = subscribable.translate(delta))

    fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestFlagSetter =
        copy(subscribable = subscribable.rotate(direction, origin))

    enum class Type(
        val negatedName: String,
        val creator: (
            activate: Set<TestSignal>,
            deactivate: Set<TestSignal>,
            SubscribableIdentifier<*>,
            defaultActive: Boolean
        ) -> TestSignalFlag
    ) {
        DISALLOW("ALLOW", TestSignalFlag::Disallow) {
            override fun getDefault(subscribable: SubscribableIdentifier<*>, planet: LookupPlanet): Boolean =
                when (subscribable) {
                    is SubscribableIdentifier.Path -> subscribable.lookup(planet).hidden
                    is SubscribableIdentifier.Node -> planet.getPaths(subscribable.coordinate).all(Path::hidden)
                }
        },
        SKIP("UNSKIP", TestSignalFlag::Skip) {
            override fun getDefault(subscribable: SubscribableIdentifier<*>, planet: LookupPlanet): Boolean = false
        };

        fun nameWithValue(value: Boolean): String = (if (value) name else negatedName).toLowerCase()

        abstract fun getDefault(subscribable: SubscribableIdentifier<*>, planet: LookupPlanet): Boolean

        companion object {
            fun fromString(name: String): Pair<Type, Boolean> {
                val canonicalName = name.trim().toUpperCase()
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
