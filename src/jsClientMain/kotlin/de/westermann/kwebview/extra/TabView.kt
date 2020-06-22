package de.westermann.kwebview.extra

import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.HTMLDivElement

class TabView : ViewCollection<View>(createHtmlView<HTMLDivElement>()) {
    override val html = super.html as HTMLDivElement

    private val tabHeaderBox = boxView("tab-view-header")
    private val tabContentBox = boxView("tab-view-content")

    private val tabList = mutableListOf<Tab>()
    fun tab(name: String, content: BoxView.() -> Unit) {
        tabList += Tab(name, content)

        if (tabList.size == 1) {
            tabList.firstOrNull()?.open()
        }
    }

    private inner class Tab(
        name: String,
        val contentInit: BoxView.() -> Unit
    ) {

        private val header = tabHeaderBox.textView(name)

        private var content: BoxView? = null

        fun open() {
            for (tab in tabList) {
                if (tab == this) continue
                tab.header.classList -= "active"
            }
            header.classList += "active"

            tabContentBox.clear()

            if (content == null) {
                content = BoxView().also(contentInit)
            }
            val content = content ?: return
            tabContentBox += content
        }

        init {
            header.onClick {
                open()
            }
        }
    }
}

@KWebViewDsl
fun ViewCollection<in TabView>.tabView(init: TabView.() -> Unit = {}): TabView {
    val view = TabView()
    append(view)
    init(view)
    return view
}
