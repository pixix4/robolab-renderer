package de.robolab.web.views

import de.robolab.app.controller.InfoBarController
import de.robolab.app.controller.TraverserBarController
import de.robolab.app.model.file.InfoBarFileEditor
import de.robolab.app.model.file.InfoBarTraverser
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.multilineInputView

class InfoBar(private val infoBarController: InfoBarController, infoBarActiveProperty: Property<Boolean>) : ViewCollection<View>() {

    private val headerView: BoxView
    private val contentView: BoxView

    private fun updateHeader() {
        headerView.clear()

        val list = infoBarController.contentListProperty.value

        if (list.size <= 1) {
            return
        }

        headerView.buttonGroup {
            for (btn in list) {
                button(btn.nameProperty) {
                    classList.bind("active", infoBarController.selectedContentProperty.mapBinding { it == btn })

                    onClick {
                        infoBarController.selectContent(btn)
                    }
                }
            }
        }
    }

    private fun updateContent() {
        contentView.clear()

        val content = infoBarController.selectedContentProperty.value ?: return

        when (content) {
            is InfoBarFileEditor -> {
                contentView.multilineInputView(content.contentProperty)
            }
            is InfoBarTraverser -> {
                if (content.traverserProperty.value == null) {
                    content.traverse()
                }
                contentView.add(NullableViewContainer(content.traverserProperty))
            }
        }
    }

    init {
        classList.bind("active", infoBarActiveProperty)

        headerView = boxView("info-bar-header") {}
        infoBarController.contentListProperty.onChange {
            updateHeader()
        }
        updateHeader()

        contentView = boxView("info-bar-content") {}
        infoBarController.selectedContentProperty.onChange {
            updateContent()
        }
        updateContent()

        // Close info bar on mobile
        onClick {
            println("asdfsad")
            console.log(it)
            if (it.target == html && infoBarActiveProperty.value) {
                println("jihrelfds")
                infoBarActiveProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class NullableViewContainer(private val traverserProperty: ReadOnlyProperty<TraverserBarController?>) : ViewCollection<View>() {
    
    private var prop: Property<TraverserBarController>? = null
    private var view: TraverserBarView? = null

    private fun updateView() {
        val traverser = traverserProperty.value

        if (traverser == null) {
            clear()
            return
        }

        if (prop == null) {
            prop = property(traverser)
        } else {
            prop?.value = traverser
        }
        if (view == null) {
            view = TraverserBarView(prop!!)
        }

        add(view!!)
    }
    
    init {
        traverserProperty.onChange {
            updateView()
        }
        updateView()
    }

}
