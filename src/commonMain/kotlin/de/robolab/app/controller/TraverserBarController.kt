package de.robolab.app.controller

import de.robolab.app.model.traverser.CharacteristicItem
import de.robolab.app.model.traverser.ITraverserStateEntry
import de.robolab.app.model.traverser.TraverserStateEntry
import de.robolab.planet.Planet
import de.robolab.traverser.*
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class TraverserBarController(val traverser: Traverser<*, *, *, *>, autoExpand: Boolean = true) {
    val autoExpandProperty: ObservableProperty<Boolean> = property(autoExpand)

    val sliceViewer: ObservableTreeSliceViewer<out ITraverserState<*>> = traverser.observableTreeSliceViewer()

    private val _currentExploredPlanet: ObservableProperty<Planet> = property(traverser.planet.planet.asUnexplored())
    val currentExploredPlanet: ObservableValue<Planet> = _currentExploredPlanet

    val isNextEnabled: ObservableValue<Boolean> = sliceViewer.hasNextProperty
    val isPreviousEnabled: ObservableValue<Boolean> = sliceViewer.hasPreviousProperty

    private val _characteristicList: ObservableMutableList<CharacteristicItem> = observableListOf()
    val characteristicList: ObservableList<CharacteristicItem> = _characteristicList

    private val _entryList: ObservableMutableList<TraverserStateEntry<*>> = observableListOf()
    val entryList: ObservableList<ITraverserStateEntry> = _entryList

    val traverserTitle: ObservableValue<String> = constObservable(traverser.name)

    val rootState: ITraverserState<*> = traverser.seed

    val currentTraverserState: ObservableValue<ITraverserState<*>> = sliceViewer.currentNodeProperty

    fun clickExpand(): Boolean = sliceViewer.expand()

    fun clickFullExpand(): Boolean = sliceViewer.fullExpand(ITraverserState<*>::running)

    fun clickNextOption(index: Int, isLeftExpand: Boolean = true): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternative(index, isLeftExpand)
            else sliceViewer.nextAlternative(index)

    fun clickPreviousOption(index: Int, isLeftExpand: Boolean = false): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternative(index, isLeftExpand)
            else sliceViewer.previousAlternative(index)

    fun clickNextOption(state: ITraverserState<*>, isLeftExpand: Boolean = true): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternative(isLeftExpand) { it.currentOption == state }
            else sliceViewer.nextAlternative { it.currentOption == state }

    fun clickPreviousOption(state: ITraverserState<*>, isLeftExpand: Boolean = false): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternative(isLeftExpand) { it.currentOption == state }
            else sliceViewer.previousAlternative { it.currentOption == state }

    fun clickNextOption(entry: TraverserStateEntry<*>, isLeftExpand: Boolean = true): Boolean =
            clickNextOption(entry.state.get(), isLeftExpand)

    fun clickPreviousOption(entry: TraverserStateEntry<*>, isLeftExpand: Boolean = false): Boolean =
            clickPreviousOption(entry.state.get(), isLeftExpand)


    fun clickNextTrail() =
            if (autoExpandProperty.value) sliceViewer.fullExpandNext()
            else sliceViewer.next()

    fun clickPreviousTrail() =
            if (autoExpandProperty.value) sliceViewer.fullExpandPrevious()
            else sliceViewer.previous()

    fun selectEntry(entry: TraverserStateEntry<*>, multiple: Boolean = false) {
        if (multiple)
            entry.selected.set(!entry.selected.value)
        else {
            var othersSelected: Boolean = false
            _entryList.filter { it != entry }.forEach { othersSelected = othersSelected || it.selected.value; it.selected.set(false) }
            if (othersSelected)
                entry.selected.set(true)
            else
                entry.selected.set(!entry.selected.value)
        }
        _currentExploredPlanet.set((_entryList.lastOrNull { it.selected.value }
                ?: _entryList.last()).currentOption.value.createExploredPlanet(traverser.planet.planet))
    }

    init {
        sliceViewer.onChange += {
            _characteristicList.clear()
            _characteristicList.addAll(CharacteristicItem.generateCharacteristic(sliceViewer.currentNode))
            _entryList.clear()
            _entryList.addAll(sliceViewer.map { TraverserStateEntry(this, it) })
            _currentExploredPlanet.set((_entryList.lastOrNull { it.selected.value }
                    ?: _entryList.last()).currentOption.value.createExploredPlanet(traverser.planet.planet))
        }
        autoExpandProperty.onChange += {
            if (autoExpandProperty.value)
                clickFullExpand()
        }
        _entryList.clear()
        _entryList.addAll(sliceViewer.map { TraverserStateEntry(this, it) })
        if (autoExpand)
            clickFullExpand()
    }
}