package de.robolab.web.views

import de.robolab.app.model.file.InfoBarTraverser
import de.robolab.app.model.traverser.CharacteristicItem
import de.robolab.app.model.traverser.ITraverserStateEntry
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.not
import de.westermann.kobserve.property.flatMapReadOnlyNullableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class TraverserBarView(private val infoBarTraverser: InfoBarTraverser) : ViewCollection<View>() {
    
    private val entryList: ReadOnlyProperty<ObservableReadOnlyList<ITraverserStateEntry>> = infoBarTraverser.traverserProperty.mapBinding {
        if (it == null) return@mapBinding observableListOf<ITraverserStateEntry>()
        it.entryList as ObservableReadOnlyList<ITraverserStateEntry>
    }


    private val characteristicList: ReadOnlyProperty<ObservableReadOnlyList<CharacteristicItem>> = infoBarTraverser.traverserProperty.mapBinding {
        if (it == null) return@mapBinding observableListOf<CharacteristicItem>()
        it.characteristicList
    }

    init {
        boxView("traverser-bar-header") {
            textView(infoBarTraverser.traverserProperty.flatMapReadOnlyNullableBinding { it?.traverserTitle }.mapBinding {
                it ?: ""
            })

            button("Run traverser") {
                onClick {
                    infoBarTraverser.traverse()
                }
            }
        }

        boxView {

            boxView {
                listFactory(entryList, factory =  { entry ->
                    TraverserEntryView(entry)
                })
            }
            boxView {
                listFactory(characteristicList, factory =  { characteristic ->
                    TraverserCharacteristicView(characteristic)
                })
            }
        }
    }
}

class TraverserEntryView(private val entry: ITraverserStateEntry): ViewCollection<View>(){

    init {
        classList.bind("selected", entry.selected)

        button {
            disabledProperty.bind(!entry.isPreviousEnabled)
            iconView(MaterialIcon.CHEVRON_LEFT)

            onClick { event ->
                entry.clickPreviousOption()
                event.stopPropagation()
            }
        }

        textView(entry.visibleTitle)

        button {
            disabledProperty.bind(!entry.isPreviousEnabled)
            iconView(MaterialIcon.CHEVRON_RIGHT)
            onClick { event ->
                entry.clickNextOption()
                event.stopPropagation()
            }
        }

        onClick {
            entry.select()
        }
    }
}

class TraverserCharacteristicView(private val item: CharacteristicItem): ViewCollection<View>(){

    init {
        style {
            backgroundColor = item.color.toString()
        }
    }
}
