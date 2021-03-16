package de.robolab.client.app.viewmodel

import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.observeConst

sealed class FormViewModel: ViewModel {

    class Entry(
        val contentProperty: ObservableValue<FormContentViewModel>,
    ) : FormViewModel() {
        constructor(
            content: FormContentViewModel,
        ) : this(
            content.observeConst(),
        )
    }

    class LabeledEntry(
        val labelProperty: ObservableValue<String>,
        val contentProperty: ObservableValue<FormContentViewModel>,
    ) : FormViewModel() {
        constructor(
            label: String,
            content: FormContentViewModel,
        ) : this(
            label.observeConst(),
            content.observeConst(),
        )
    }

    class Group(
        val contentProperty: ObservableMutableList<FormViewModel>,
    ) : FormViewModel() {
        constructor(
            vararg content: FormViewModel,
        ) : this(
            observableListOf(*content),
        )
    }

    class LabeledGroup(
        val labelProperty: ObservableValue<String>,
        val contentProperty: ObservableMutableList<FormViewModel>,
    ) : FormViewModel() {
        constructor(
            label: String,
            vararg content: FormViewModel,
        ) : this(
            label.observeConst(),
            observableListOf(*content),
        )
    }
}
