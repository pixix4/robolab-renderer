package de.robolab.app.model.traverser

import de.robolab.app.controller.TraverserBarController
import de.robolab.traverser.ITraverserState
import de.robolab.traverser.TreeSliceViewer
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.*
import de.robolab.traverser.property
import de.westermann.kobserve.or

interface ITraverserStateEntry {
    val sliceEntry: ReadOnlyProperty<out TreeSliceViewer.TreeSliceEntry<out ITraverserState<*>>>
    val isNextEnabled: ReadOnlyProperty<Boolean>
    val isPreviousEnabled: ReadOnlyProperty<Boolean>
    val areAlternativeButtonsVisible: ReadOnlyProperty<Boolean>
    fun clickNextOption()
    fun clickPreviousOption()
    fun select() = select(false)
    fun select(multiple: Boolean = false)
    val currentOptionIndex: ReadOnlyProperty<Int>
    val optionCount: ReadOnlyProperty<Int>
    val currentOption: ReadOnlyProperty<out ITraverserState<*>>
    val options: ReadOnlyProperty<out List<ITraverserState<*>>>
    val state: ReadOnlyProperty<out ITraverserState<*>>
    val selected: ReadOnlyProperty<Boolean>
    val visibleTitle: ReadOnlyProperty<String>
    val visibleDetails: ReadOnlyProperty<List<String>>
    val defaultTitle: ReadOnlyProperty<String>
    val selectedTitle: ReadOnlyProperty<String>
    val details: ReadOnlyProperty<List<String>>
}

class TraverserStateEntry<TS>(val controller: TraverserBarController, sliceEntry: TreeSliceViewer.TreeSliceEntry<out TS>) : ITraverserStateEntry where TS : ITraverserState<*> {
    override fun clickNextOption() = controller.clickNextOption(this).let { }
    override fun clickPreviousOption() = controller.clickPreviousOption(this).let { }
    override val sliceEntry: Property<TreeSliceViewer.TreeSliceEntry<out TS>> = property(sliceEntry)
    override val isNextEnabled: ReadOnlyProperty<Boolean> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::hasNext)
    override val isPreviousEnabled: ReadOnlyProperty<Boolean> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::hasPrevious)
    override val areAlternativeButtonsVisible: ReadOnlyProperty<Boolean> = this.isNextEnabled or this.isPreviousEnabled
    override val currentOptionIndex: ReadOnlyProperty<Int> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::currentIndex)
    override val currentOption: ReadOnlyProperty<TS> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::currentOption)

    override val options: ReadOnlyProperty<List<TS>> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::options)
    //TODO: use ObservableReadOnlyList<TS> with this.sliceEntry.flatMapReadOnlyBinding

    override val optionCount: ReadOnlyProperty<Int> = this.options.mapBinding(List<ITraverserState<*>>::size)
    override val state: ReadOnlyProperty<out ITraverserState<*>> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::currentOption)
    override fun select(multiple: Boolean) = controller.selectEntry(this, multiple)
    override val selected: Property<Boolean> = property(false)

    override val selectedTitle: ReadOnlyProperty<String> = this.sliceEntry.mapBinding {
        with(it) {
            "${currentOption.location.x}, ${currentOption.location.y} --> ${currentOption.nextDirection}   [${currentIndex + 1}/${options.size}]"
        }
    }
    override val defaultTitle: ReadOnlyProperty<String> = this.sliceEntry.mapBinding {
        with(it) {
            (if (currentOption.nextDirection != null) "${currentOption.nextDirection}"
            else "${currentOption.location.x}, ${currentOption.location.y}") +
                    if (options.size != 1) "   [${currentIndex + 1}/${options.size}]"
                    else ""
        }
    }
    override val details: ReadOnlyProperty<List<String>> = this.sliceEntry.mapBinding {
        with(it.currentOption) {
            listOf("depth: $depth",
                    "status: $status",
                    "statusInfo: $statusInfo")
        }
    }
    override val visibleTitle: ReadOnlyProperty<String> = property({
        if (selected.value) selectedTitle.value
        else defaultTitle.value
    }, selected, defaultTitle, selectedTitle)
    override val visibleDetails: ReadOnlyProperty<List<String>> = selected.join(details) { sel, det -> if (sel) det else emptyList() }
}