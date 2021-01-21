package de.robolab.client.ui.views.utils

import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView


@KWebViewDsl
fun ViewCollection<in BoxView>.buttonGroup(formGroup: Boolean = false, init: BoxView.() -> Unit = {}): BoxView {
    val view = boxView("button-group", init = init)

    if (formGroup) view.classList += "button-form-group"

    return view
}
