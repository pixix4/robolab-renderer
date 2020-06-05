package de.robolab.client.web

import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.MainController
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.web.views.*
import de.robolab.common.utils.toDashCase
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.init
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.files.File
import kotlin.browser.document

fun main() {

    val debugView = if (PreferenceStorage.debugMode) {
        createDebugView().also {
            document.body?.appendChild(it.html)
        }
    } else null

    val mainController = MainController()

    watchSystemTheme()
    property(document::title).bind(mainController.applicationTitleProperty)

    init {
        clear()
        if (debugView != null) {
            add(debugView)
        }

        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toDashCase() })

        val toolBar = ToolBar(mainController.toolBarController)
        +toolBar
        +NavigationBar(
            mainController.navigationBarController,
            mainController.fileImportController,
            toolBar.navigationBarActiveProperty
        )
        +StatusBar(mainController.statusBarController, toolBar.navigationBarActiveProperty)
        +MainCanvas(mainController.canvasController, toolBar.navigationBarActiveProperty, toolBar.infoBarActiveProperty)
        +InfoBar(mainController.infoBarController, toolBar.infoBarActiveProperty)

        onDragOver { event ->
            event.stopPropagation()
            event.preventDefault()

            event.dataTransfer?.dropEffect = "copy"
        }

        onDrop { event ->
            event.stopPropagation()
            event.preventDefault()
            val files = event.dataTransfer?.files?.let { fileList ->
                (0 until fileList.length).map { fileList.item(it)!! }
            } ?: emptyList()

            GlobalScope.launch(Dispatchers.Default) {
                for (file in files) {
                    val content = file.readText()
                    if (content != null) {
                        mainController.fileImportController.importFile(file.name, content)
                    }
                }
            }
        }
    }
}

@Suppress("UnsafeCastFromDynamic")
fun createDebugView(): BoxView {
    val box = BoxView()
    box.classList += "debug-terminal"

    val native: (HTMLElement) -> Unit = js(
        """
        function(html) {
            if (typeof console != "undefined") {
                if (typeof console.log != 'undefined') {
                    console.olog = console.log;
                } else {
                    console.olog = function () {};
                }
            }

            console.log = function () {
                console.olog.apply(null, arguments);
                var message = Array.prototype.slice.call(arguments).join(" ");
                message = message.replace(/%c/g, "");
                message = message.replace(/ color: initial/g, "");
                message = message.replace(/ color: [#0-9a-zA-Z]+/g, "");
                
                var pre = document.createElement("pre");
                pre.textContent = message;
                html.appendChild(pre);
            };
            console.error = console.debug = console.info = console.log;
        }
    """
    )

    native(box.html)
    return box
}
