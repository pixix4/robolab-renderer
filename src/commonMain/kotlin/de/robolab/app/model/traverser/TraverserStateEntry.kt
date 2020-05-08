package de.robolab.app.model.traverser

import de.robolab.app.controller.TraverserBarController
import de.robolab.traverser.ITraverserState
import de.robolab.traverser.TreeSliceViewer
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.or
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

interface ITraverserStateEntry {
    val sliceEntry: ObservableValue<TreeSliceViewer.TreeSliceEntry<out ITraverserState<*>>>
    val isNextEnabled: ObservableValue<Boolean>
    val isPreviousEnabled: ObservableValue<Boolean>
    val areAlternativeButtonsVisible: ObservableValue<Boolean>
    fun clickNextOption()
    fun clickPreviousOption()
    fun select() = select(false)
    fun select(multiple: Boolean = false)
    val currentOptionIndex: ObservableValue<Int>
    val optionCount: ObservableValue<Int>
    val currentOption: ObservableValue<ITraverserState<*>>
    val options: ObservableValue<List<ITraverserState<*>>>
    val state: ObservableValue<ITraverserState<*>>
    val selected: ObservableValue<Boolean>
    val visibleTitle: ObservableValue<String>
    val visibleDetails: ObservableValue<List<String>>
    val defaultTitle: ObservableValue<String>
    val selectedTitle: ObservableValue<String>
    val details: ObservableValue<List<String>>
}

class TraverserStateEntry<TS>(val controller: TraverserBarController, sliceEntry: TreeSliceViewer.TreeSliceEntry<out TS>) : ITraverserStateEntry where TS : ITraverserState<*> {
    override fun clickNextOption() = controller.clickNextOption(this).let { if (!it) error("Could not select next option") }
    override fun clickPreviousOption() = controller.clickPreviousOption(this, isLeftExpand = true).let { if (!it) error("Could not select previous option") }

    //Feels weird if selecting previous option full-expands to the right
    override val sliceEntry: ObservableProperty<TreeSliceViewer.TreeSliceEntry<out TS>> = property(sliceEntry)
    override val isNextEnabled: ObservableValue<Boolean> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::hasNext)
    override val isPreviousEnabled: ObservableValue<Boolean> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::hasPrevious)
    override val areAlternativeButtonsVisible: ObservableValue<Boolean> = this.isNextEnabled or this.isPreviousEnabled
    override val currentOptionIndex: ObservableValue<Int> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<*>::currentIndex)
    override val currentOption: ObservableValue<TS> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::currentOption)

    override val options: ObservableValue<List<TS>> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::options)
    //TODO: use ObservableReadOnlyList<TS> with this.sliceEntry.flatMapReadOnlyBinding

    override val optionCount: ObservableValue<Int> = this.options.mapBinding(List<ITraverserState<*>>::size)
    override val state: ObservableValue<ITraverserState<*>> = this.sliceEntry.mapBinding(TreeSliceViewer.TreeSliceEntry<out TS>::currentOption)
    override fun select(multiple: Boolean) = controller.selectEntry(this, multiple)
    override val selected: ObservableProperty<Boolean> = property(false)

    override val selectedTitle: ObservableValue<String> = this.sliceEntry.mapBinding {
        with(it) {
            "${currentOption.location.x}, ${currentOption.location.y} --> ${currentOption.nextDirection}   [${currentIndex + 1}/${options.size}]"
        }
    }
    override val defaultTitle: ObservableValue<String> = this.sliceEntry.mapBinding {
        with(it) {
            (if (!currentOption.running)
                "${currentOption.status}" + (if (currentOption.statusInfo != null && currentOption.statusInfo !is Throwable) " (${currentOption.statusInfo})" else "")
            else if (currentOption.nextDirection != null) "${currentOption.nextDirection}"
            else "${currentOption.location.x}, ${currentOption.location.y}") +
                    (if (options.size != 1) "   [${currentIndex + 1}/${options.size}]"
                    else "")
        }
    }
    override val details: ObservableValue<List<String>> = this.sliceEntry.mapBinding {
        with(it.currentOption) {
            listOf("depth: $depth",
                    "status: $status",
                    "statusInfo: $statusInfo")
        }
    }
    override val visibleTitle: ObservableValue<String> = property(selected, defaultTitle, selectedTitle) {
        if (selected.value) selectedTitle.value
        else defaultTitle.value
    }
    override val visibleDetails: ObservableValue<List<String>> = selected.join(details) { sel, det -> if (sel) det else emptyList() }
}