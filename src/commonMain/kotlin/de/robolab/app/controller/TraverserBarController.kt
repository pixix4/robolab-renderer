package de.robolab.app.controller

import de.robolab.app.model.traverser.*
import de.robolab.traverser.*
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

class TraverserBarController(val traverser: Traverser<*, *, *, *>, autoExpand: Boolean = true) {
    val autoExpandProperty: Property<Boolean> = property(autoExpand)

    val sliceViewer: ObservableTreeSliceViewer<out ITraverserState<*>> = traverser.observableTreeSliceViewer()

    val isNextEnabled: ReadOnlyProperty<Boolean> = sliceViewer.hasNextProperty
    val isPreviousEnabled: ReadOnlyProperty<Boolean> = sliceViewer.hasPreviousProperty

    private val _characteristicList: ObservableList<CharacteristicItem> = observableListOf()
    val characteristicList: ObservableReadOnlyList<CharacteristicItem> = _characteristicList

    private val _entryList: ObservableList<TraverserStateEntry<*>> = observableListOf()
    val entryList: ObservableReadOnlyList<out ITraverserStateEntry> = _entryList

    val traverserTitle: ReadOnlyProperty<String> = constProperty(traverser.name)

    val rootState: ITraverserState<*> = traverser.seed

    val currentTraverserState: ReadOnlyProperty<out ITraverserState<*>> = sliceViewer.currentNodeProperty

    fun clickExpand(): Boolean = sliceViewer.expand()

    fun clickFullExpand(): Boolean = sliceViewer.fullExpand(ITraverserState<*>::running)

    fun clickNextOption(index: Int): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternative(index)
            else sliceViewer.nextAlternative(index)

    fun clickPreviousOption(index: Int): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternative(index)
            else sliceViewer.previousAlternative(index)

    fun clickNextOption(state: ITraverserState<*>): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternative { it.currentOption == state}
            else sliceViewer.nextAlternative { it.currentOption == state }

    fun clickPreviousOption(state: ITraverserState<*>): Boolean =
            if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternative { it.currentOption == state }
            else sliceViewer.previousAlternative { it.currentOption == state }

    fun clickNextOption(entry: TraverserStateEntry<*>): Boolean =
            clickNextOption(entry.state.get())

    fun clickPreviousOption(entry: TraverserStateEntry<*>): Boolean =
            clickPreviousOption(entry.state.get())


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
    }

    init {
        sliceViewer.onChange += {
            _characteristicList.clear()
            _characteristicList.addAll(CharacteristicItem.generateCharacteristic(sliceViewer.currentNode))
            _entryList.clear()
            _entryList.addAll(sliceViewer.map { TraverserStateEntry(this, it) })
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