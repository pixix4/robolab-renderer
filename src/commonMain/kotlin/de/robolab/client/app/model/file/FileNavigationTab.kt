package de.robolab.client.app.model.file

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.RemoteIdentifier
import de.robolab.client.app.model.file.provider.RemoteMode
import de.robolab.client.app.model.group.GroupNavigationList
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationTab(
    private val contentController: ContentController,
    private val filePlanetController: FilePlanetController,
    private val loader: IFilePlanetLoader,
    private val uiController: UiController
) : INavigationBarTab(
    loader.nameProperty.join(loader.descProperty) { name, desc ->
        "$name: $desc"
    },
    loader.iconProperty,
) {


    override val contentProperty = property<SideBarContentViewModel>(
        FileNavigationList(this, loader, filePlanetController)
    )

    private val searchProperty = property("")

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent {
        input(searchProperty, typeHint = FormContentViewModel.InputTypeHint.SEARCH)
        button(MaterialIcon.ADD, description = "Open planet file") {
            // TODO
        }
    }


    fun createSearchList(parent: INavigationBarList): INavigationBarSearchList {
        return FileNavigationSearchList(
            this,
            loader,
            contentProperty.value,
            filePlanetController
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        when (entry) {
            is FileNavigationList.EntryPlanet -> {
                contentController.openDocument(
                    FilePlanetDocument(
                        filePlanetController.getFilePlanet(loader, entry.id),
                        uiController
                    ),
                    asNewTab
                )
            }
            is FileNavigationList.EntryDirectory -> {
                contentProperty.value = FileNavigationList(this, loader, filePlanetController, entry.id, entry.metadata, contentProperty.value)
            }
        }
    }

    init {
        loader.onRemoteChange {
            @Suppress("UNCHECKED_CAST")
            (contentProperty.value as? RepositoryList)?.onChange(it)
        }

        loader.remoteModeProperty.onChange {
            contentProperty.value = FileNavigationList(this, loader, filePlanetController)
        }
    }

    interface LoaderEventListener {

        val loader: IFilePlanetLoader
        fun onChange(entry: RemoteIdentifier)
    }

    interface RepositoryList : INavigationBarList, LoaderEventListener

}
