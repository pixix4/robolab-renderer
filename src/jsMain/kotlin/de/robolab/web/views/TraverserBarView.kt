package de.robolab.web.views

import de.robolab.app.controller.TraverserBarController
import de.robolab.app.model.file.InfoBarTraverser
import de.robolab.app.model.traverser.CharacteristicItem
import de.robolab.app.model.traverser.ITraverserStateEntry
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.flatMapReadOnlyNullableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.textView
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
        boxView {
            button("Traverse") {
                onClick {
                    infoBarTraverser.traverse()
                }
            }
        }

        boxView {
            textView(infoBarTraverser.traverserProperty.flatMapReadOnlyNullableBinding { it?.traverserTitle }.mapBinding {
                it ?: ""
            })

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

        textView(entry.defaultTitle){
            style {
                paddingRight = "1rem"
            }
        }
        textView(entry.selectedTitle){
            style {
                paddingRight = "1rem"
            }
        }
        textView(entry.visibleTitle)

        onClick {
            entry.select()
        }

        style {
            borderBottom = "solid 1px black"
        }
    }
}

class TraverserCharacteristicView(private val item: CharacteristicItem): ViewCollection<View>(){

    init {
        style {
            display = "inline-block"
            width = "1rem"
            height = "1rem"
            backgroundColor = item.color.toString()
            margin = "0.2rem"
        }
    }
}
