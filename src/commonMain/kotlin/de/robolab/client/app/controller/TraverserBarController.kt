package de.robolab.client.app.controller

import de.robolab.client.app.model.traverser.CharacteristicItem
import de.robolab.client.app.model.traverser.ITraverserStateEntry
import de.robolab.client.app.model.traverser.TraverserStateEntry
import de.robolab.client.net.RobolabScope
import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.client.traverser.*
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class TraverserBarController(val traverser: Traverser<*, *, *, *>, autoExpand: Boolean = true) {
    val autoExpandProperty: ObservableProperty<Boolean> = property(autoExpand)

    val sliceViewer: ObservableTreeSliceViewer<out ITraverserState<*>> = traverser.observableTreeSliceViewer()

    private val _renderState: ObservableProperty<TraverserRenderState?> = property()
    val renderState: ObservableValue<TraverserRenderState?> = _renderState

    val isNextEnabled: ObservableValue<Boolean> = sliceViewer.hasNextProperty
    val isPreviousEnabled: ObservableValue<Boolean> = sliceViewer.hasPreviousProperty

    private val _characteristicList: ObservableMutableList<CharacteristicItem> = observableListOf()
    val characteristicList: ObservableList<CharacteristicItem> = _characteristicList

    private val _entryList: ObservableMutableList<TraverserStateEntry<*>> = observableListOf()
    val entryList: ObservableList<ITraverserStateEntry> = _entryList

    val traverserTitle: ObservableValue<String> = constObservable(traverser.name)

    val rootState: ITraverserState<*> = traverser.seed

    val currentTraverserState: ObservableValue<ITraverserState<*>> = sliceViewer.currentNodeProperty

    private val _mutex: Mutex = Mutex()
    private val _busy: ObservableProperty<Boolean> = property(false)
    val busy: ObservableValue<Boolean> = _busy

    private suspend fun <T> withBusy(block: suspend () -> T): T {
        _mutex.lock()
        try {
            _busy.set(true)
            return block()
        } finally {
            _busy.set(false)
            _mutex.unlock()
        }
    }

    private fun <T> launchWithBusy(block: suspend () -> T) {
        RobolabScope.launch { withBusy(block) }
    }

    fun clickExpand(): Boolean = sliceViewer.expand()

    fun clickFullExpand() = launchWithBusy { sliceViewer.fullExpandAsync(ITraverserState<*>::running) }

    suspend fun clickNextOption(index: Int, isLeftExpand: Boolean = true): Boolean = withBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternativeAsync(index, isLeftExpand)
        else sliceViewer.nextAlternative(index)
    }

    suspend fun clickPreviousOption(index: Int, isLeftExpand: Boolean = false): Boolean = withBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternativeAsync(index, isLeftExpand)
        else sliceViewer.previousAlternative(index)
    }

    suspend fun clickNextOption(state: ITraverserState<*>, isLeftExpand: Boolean = true): Boolean = withBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandNextAlternativeAsync(isLeftExpand) { it.currentOption == state }
        else sliceViewer.nextAlternative { it.currentOption == state }
    }

    suspend fun clickPreviousOption(state: ITraverserState<*>, isLeftExpand: Boolean = false): Boolean = withBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAlternativeAsync(isLeftExpand) { it.currentOption == state }
        else sliceViewer.previousAlternative { it.currentOption == state }
    }

    suspend fun clickNextOption(entry: TraverserStateEntry<*>, isLeftExpand: Boolean = true): Boolean {
        val selected = entry.selected.value
        val index = if (selected) entryList.indexOf(entry) else -1
        val result = clickNextOption(entry.state.get(), isLeftExpand)
        if (selected)
            entryList.getOrNull(index)?.select()
        return result
    }

    suspend fun clickPreviousOption(entry: TraverserStateEntry<*>, isLeftExpand: Boolean = false): Boolean {
        val selected = entry.selected.value
        val index = if (selected) entryList.indexOf(entry) else -1
        val result = clickPreviousOption(entry.state.get(), isLeftExpand)
        if (selected)
            entryList.getOrNull(index)?.select()
        return result
    }

    fun keyDown() {
        val index = entryList.indexOfFirst { it.selected.value } + 1
        if (index >= entryList.size) clickExpand()
        entryList.getOrNull(index)?.select()
    }

    fun keyUp() {
        val index = entryList.indexOfFirst { it.selected.value } - 1
        entryList.getOrNull(index)?.select()
    }

    fun keyLeft() = launchWithBusy {
        val entry = entryList.firstOrNull { it.selected.value }
        if (entry != null && entry.isPreviousEnabled.value)
            entry.clickPreviousOption()
    }

    fun keyRight() = launchWithBusy {
        val entry = entryList.firstOrNull { it.selected.value }
        if (entry != null && entry.isNextEnabled.value)
            entry.clickNextOption()
    }

    fun clickNextTrail() = launchWithBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandNextAsync()
        else sliceViewer.next()
    }

    fun clickPreviousTrail() = launchWithBusy {
        if (autoExpandProperty.value) sliceViewer.fullExpandPreviousAsync()
        else sliceViewer.previous()
    }

    fun selectEntry(entry: TraverserStateEntry<*>, multiple: Boolean = false) {
        if (multiple)
            entry.selected.set(!entry.selected.value)
        else {
            var othersSelected: Boolean = false
            _entryList.filter { it != entry }
                .forEach { othersSelected = othersSelected || it.selected.value; it.selected.set(false) }
            if (othersSelected)
                entry.selected.set(true)
            else
                entry.selected.set(!entry.selected.value)
        }
        _renderState.set((_entryList.lastOrNull { it.selected.value }
            ?: _entryList.last()).currentOption.value.createRenderState(traverser.planet.planet))
    }

    init {
        _entryList.onChange += {
            _renderState.set((_entryList.lastOrNull { it.selected.value }
                ?: _entryList.last()).currentOption.value.createRenderState(traverser.planet.planet))
        }
        sliceViewer.observableEntries.onAddIndex += {
            _entryList.add(it.index, TraverserStateEntry(this, it.element))
        }
        sliceViewer.observableEntries.onRemoveIndex += {
            _entryList.removeAt(it.index)
        }
        sliceViewer.observableEntries.onSetIndex += {
            _entryList[it.index] = TraverserStateEntry(this, it.newElement)
        }
        sliceViewer.onChange += {
            _characteristicList.clear()
            _characteristicList.addAll(CharacteristicItem.createCharacteristicTrace(sliceViewer.currentNode))
        }
        autoExpandProperty.onChange += {
            if (autoExpandProperty.value)
                RobolabScope.launch {
                    clickFullExpand()
                }
        }
        _entryList.clear()
        _entryList.addAll(sliceViewer.map { TraverserStateEntry(this, it) })
        if (autoExpand)
            RobolabScope.launch {
                clickFullExpand()
            }
    }
}