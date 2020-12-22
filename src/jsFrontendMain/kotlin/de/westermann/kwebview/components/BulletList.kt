package de.westermann.kwebview.components

import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.HTMLUListElement

class BulletList : ViewCollection<ListItem>(createHtmlView<HTMLUListElement>("ul")) {
    override val html = super.html as HTMLUListElement
}

@KWebViewDsl
fun ViewCollection<in BulletList>.bulletList(vararg classes: String, init: BulletList.() -> Unit = {}): BulletList {
    val view = BulletList()
    for (c in classes) {
        view.classList += c
    }
    append(view)
    init(view)
    return view
}
