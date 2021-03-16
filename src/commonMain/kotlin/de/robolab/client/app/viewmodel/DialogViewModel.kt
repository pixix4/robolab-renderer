package de.robolab.client.app.viewmodel

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.constObservable

abstract class DialogViewModel(
    val title: ObservableValue<String>
): ViewModel {

    private var mainViewModel: MainViewModel? = null

    constructor(title: String) : this(constObservable(title))

    val onOpen = EventHandler<Unit>()
    val onClose = EventHandler<Unit>()

    fun close() {
        val main = mainViewModel ?: return

        main.dialogList -= this
        onClose.emit()

        mainViewModel = null
    }

    fun open(main: MainViewModel) {
        close()

        main.dialogList += this
        onOpen.emit()

        mainViewModel = main
    }
}
