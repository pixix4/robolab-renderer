package de.robolab.client.web.views.utils

import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView


@KWebViewDsl
fun ViewCollection<in BoxView>.buttonGroup(init: BoxView.() -> Unit = {}): BoxView {
    return boxView("button-group", init = init)
}
