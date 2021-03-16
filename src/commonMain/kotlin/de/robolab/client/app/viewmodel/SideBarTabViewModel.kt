package de.robolab.client.app.viewmodel

import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

abstract class SideBarTabViewModel(
    val nameProperty: ObservableValue<String>,
    val iconProperty: ObservableValue<MaterialIcon>
): ViewModel {

    constructor(name: String, icon: MaterialIcon): this(
        constObservable(name),
        constObservable(icon)
    )

    abstract val contentProperty: ObservableValue<SideBarContentViewModel>

    open fun onNavigateBack() {
        val property = contentProperty
        val contentParent = contentProperty.value.parent
        if (contentParent != null && property is ObservableProperty) {
            property.value = contentParent
        }
    }

    abstract val topToolBar: FormContentViewModel.Group
    abstract val bottomToolBar: FormContentViewModel.Group
}
