package de.robolab.client.ui

import de.robolab.client.app.controller.MainController
import de.robolab.client.app.viewmodel.MainViewModel
import de.robolab.client.ui.views.*
import de.robolab.client.ui.views.boxes.*
import de.robolab.client.ui.views.dialogs.*
import de.robolab.client.ui.views.utils.FormContentView
import de.robolab.client.ui.views.utils.FormView
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.electron
import de.robolab.common.utils.toDashCase
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.bindStyleProperty
import de.westermann.kwebview.components.init
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

fun initMainView(
    args: MainController.Args
) {
    ViewFactoryRegistry.register(MainView)
    ViewFactoryRegistry.register(ToolBarView)
    ViewFactoryRegistry.register(StatusBarView)
    ViewFactoryRegistry.register(ContentView)
    ViewFactoryRegistry.register(TerminalInputView)
    ViewFactoryRegistry.register(TerminalView)

    ViewFactoryRegistry.register(FormContentView)
    ViewFactoryRegistry.register(FormView)

    ViewFactoryRegistry.register(ExportPlanetDialogView)
    ViewFactoryRegistry.register(SendMessageDialogView)
    ViewFactoryRegistry.register(SettingsDialogView)
    ViewFactoryRegistry.register(TransformPlanetDialogView)
    ViewFactoryRegistry.register(TokenDialogView)

    ViewFactoryRegistry.register(SideBarView)
    ViewFactoryRegistry.register(SideBarNavigationView)
    ViewFactoryRegistry.register(InfoBarFileEditView)
    ViewFactoryRegistry.register(InfoBarFilePaperView)
    ViewFactoryRegistry.register(InfoBarFileTestView)
    ViewFactoryRegistry.register(InfoBarFileTraverseView)
    ViewFactoryRegistry.register(InfoBarFileViewView)
    ViewFactoryRegistry.register(InfoBarGroupMessagesView)
    ViewFactoryRegistry.register(InfoBarRoomRobotsView)

    ViewFactoryRegistry.register(DetailBoxPlanetStatisticsView)
    ViewFactoryRegistry.register(DetailBoxPathView)
    ViewFactoryRegistry.register(DetailBoxPointView)

    val mainController = MainController(args)

    watchSystemTheme()
    property(document::title).bind(mainController.applicationTitleProperty)

    init {
        val uiController = mainController.uiController
        dataset.bind("theme", PreferenceStorage.selectedThemeProperty.mapBinding { it.name.toDashCase() })
        dataset.bind("dark", PreferenceStorage.selectedThemeProperty.mapBinding { (it.isDarkMode == true).toString() })

        bindStyleProperty(
            "--left-side-bar-view-width",
            uiController.navigationBarWidthProperty.join(uiController.navigationBarVisibleProperty) { width, visible ->
                if (visible) "${width}px" else "0px"
            })
        bindStyleProperty(
            "--right-side-bar-view-width",
            uiController.infoBarWidthProperty.join(uiController.infoBarVisibleProperty) { width, visible ->
                if (visible) "${width}px" else "0px"
            })


        clear()
        +ViewFactoryRegistry.create(MainViewModel(mainController)) {
            classList.bind("left-side-bar-view-active", uiController.navigationBarVisibleProperty)
            classList.bind("right-side-bar-view-active", uiController.infoBarVisibleProperty)
            classList.bind("fullscreen", uiController.fullscreenProperty)
        }

        onDragOver { event ->
            event.stopPropagation()
            event.preventDefault()

            event.dataTransfer?.dropEffect = "copy"
        }

        onDrop { event ->
            event.stopPropagation()
            event.preventDefault()
            val files = event.dataTransfer?.files?.let { fileList ->
                (0 until fileList.length).mapNotNull { fileList.item(it) }
            } ?: emptyList()

            GlobalScope.launch(Dispatchers.Default) {
                for (file in files) {
                    val content = file.readText()
                    if (content != null) {
                        mainController.fileImportController.importFile(
                            file.pathOrName(),
                            Instant.fromEpochMilliseconds("${file.lastModified}".toLong())
                        ) { content.splitToSequence("\n") }
                    }
                }
            }
        }

        mainController.finishSetup()

        onKeyDown { event ->
            /*if (event.ctrlOrCommandKey && event.key == "w") {
                val tab = mainController.tabController.activeTabProperty.value

                if (tab != null && !mainController.tabController.empty()) {
                    event.stopPropagation()
                    event.preventDefault()

                    tab.close()
                }
            }

            if (event.ctrlOrCommandKey && event.key == "t") {
                event.stopPropagation()
                event.preventDefault()

                mainController.tabController.openNewTab()
            }*/
        }

        electron { electron ->
            electron.ipcRenderer.on("open-file") { _, args ->
                val name = args.name.unsafeCast<String>()
                val lastModified = args.mtime.unsafeCast<Double>()
                val content = args.content.unsafeCast<String>()

                GlobalScope.launch(Dispatchers.Default) {
                    mainController.fileImportController.importFile(
                        name,
                        Instant.fromEpochMilliseconds(lastModified.toLong())
                    ) { content.splitToSequence("\n") }
                }
            }
        }
    }
}
