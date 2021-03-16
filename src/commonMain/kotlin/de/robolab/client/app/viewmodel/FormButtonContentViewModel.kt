package de.robolab.client.app.viewmodel

import de.robolab.client.app.model.base.MaterialIcon

sealed class FormButtonContentViewModel {
    class Icon(val icon: MaterialIcon): FormButtonContentViewModel()
    class Text(val text: String): FormButtonContentViewModel()

    companion object {
        operator fun invoke(icon: MaterialIcon) = Icon(icon)
        operator fun invoke(text: String) = Text(text)
    }
}
