package de.robolab.common.utils.dsl.regex.components

import de.robolab.common.utils.dsl.regex.CharacterClass

abstract class ComponentWithRegex(components: List<IRegexComponent> = emptyList()) : IRegexComponent {

    val components: MutableList<IRegexComponent> = components.toMutableList()

    private fun <T> addAndRun(component: T, block: T.() -> Unit): T where T : IRegexComponent =
        component.also(block).also { components.add(it) }


    operator fun String.unaryPlus() = literal(this)
    operator fun Regex.unaryPlus() = compiled(this)
    operator fun CharacterClass.unaryPlus() {
        characterClass {
            +this@unaryPlus
        }
    }


    fun literal(literal: String) {
        components.add(LiteralComponent(literal))
    }

    fun compiled(regex: Regex) {
        components.add(CompiledComponent(regex))
    }

    fun compiled(regex: String) = compiled(regex.toRegex())

    fun capture(block: CaptureComponent.() -> Unit) = addAndRun(CaptureComponent(), block)
    //fun capture(name: String, block: CaptureComponent.() -> Unit) = addAndRun(CaptureComponent(name), block)

    fun group(block: GroupComponent.() -> Unit) = addAndRun(GroupComponent(), block)

    fun alternative(block: AlternativeComponent.() -> Unit) = addAndRun(AlternativeComponent(), block)
    fun alternative(explicitGroup: Boolean, block: AlternativeComponent.() -> Unit) =
        addAndRun(AlternativeComponent(explicitGroup = explicitGroup), block)

    fun empty() {
        components.add(EmptyComponent)
    }

    fun epsilon() {
        components.add(EpsilonComponent)
    }

    fun unmatchable() {
        components.add(UnmatchableComponent)
    }

    fun optional(block: MultipleComponent.() -> Unit) = maybe(block)

    fun maybe(block: MultipleComponent.() -> Unit) = addAndRun(MultipleComponent(0, 1, explicit = false), block)
    fun atLeastOne(block: MultipleComponent.() -> Unit) =
        addAndRun(MultipleComponent(1, Int.MAX_VALUE, explicit = false), block)

    fun anyCount(block: MultipleComponent.() -> Unit) =
        addAndRun(MultipleComponent(0, Int.MAX_VALUE, explicit = false), block)

    fun multiple(block: MultipleComponent.() -> Unit) = atLeastOne(block)
    fun multiple(count: Int, block: MultipleComponent.() -> Unit) =
        addAndRun(MultipleComponent(count, count, explicit = true), block)

    fun multiple(minimum: Int, maximum: Int, block: MultipleComponent.() -> Unit) =
        addAndRun(MultipleComponent(minimum, maximum, explicit = true), block)

    fun characterClass(block: CharacterClassComponent.() -> Unit) = addAndRun(CharacterClassComponent(), block)
    fun characterClass(negated: Boolean = false, block: CharacterClassComponent.() -> Unit) =
        addAndRun(CharacterClassComponent(negated), block)

    fun anyCharacter() {
        components.add(AnyCharacterComponent)
    }

}