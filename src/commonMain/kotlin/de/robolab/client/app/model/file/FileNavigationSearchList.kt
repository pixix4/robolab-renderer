package de.robolab.client.app.model.file

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarSearchList
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationSearchList(
    private val tab: FileNavigationTab,
    val loader: IFilePlanetLoader,
    override val parent: SideBarContentViewModel,
    private val filePlanetController: FilePlanetController,
) : INavigationBarSearchList {

    override val nameProperty = constObservable("Search")

    override val searchProperty = property("")

    override val childrenProperty = observableListOf<INavigationBarEntry>()

    init {
        searchProperty.onChange {
            GlobalScope.launch {
                val planets = loader.searchPlanets(searchProperty.value) ?: emptyList()

                runAsync {
                    childrenProperty.sync(planets.map {
                        FileNavigationList.mapEntry(tab, null, loader, it, filePlanetController)
                    })
                }
            }
        }
    }
}
