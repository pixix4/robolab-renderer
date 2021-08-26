package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.client.traverser.DefaultTraverser
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.*
import de.westermann.kobserve.toggle

class InfoBarFileTraverse(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
) : FilePlanetDocument.FilePlanetSideBarTab<LivePlanetDrawable>(
    "Traverse",
    MaterialIcon.CALL_SPLIT
), SideBarContentViewModel {

    val traverserProperty = property<TraverserBarController?>(null)

    override val drawable = LivePlanetDrawable(planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importBackgroundPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent {
        label(traverserProperty.nullableFlatMapBinding { it?.traverserTitle }.mapBinding { it ?: "" })
        button(MaterialIcon.REFRESH) {
            //traverserProperty.value.rerun()
        }
    }
    override val bottomToolBar = buildFormContent {
        group {
            button(
                MaterialIcon.CHEVRON_LEFT,
                enabledProperty = traverserProperty.nullableFlatMapBinding { it?.isPreviousEnabled }.mapBinding { it ?: false }
            ) {
                traverserProperty.value?.clickPreviousTrail()
            }
        }
        group {
            label(traverserProperty.nullableFlatMapBinding { it?.traverserTitle }.mapBinding { it ?: "" })
        }
        group {
            button(
                MaterialIcon.ARROW_DROP_DOWN,
                enabledProperty = traverserProperty.nullableFlatMapBinding { it?.autoExpandProperty }
                    .mapBinding { it?.not() ?: false }
            ) {
                if (it.shiftKey) {
                    traverserProperty.value?.clickExpand()
                } else {
                    traverserProperty.value?.clickFullExpand()
                }
            }
            button(MaterialIcon.ARROW_DOWNWARD) {
                traverserProperty.value?.autoExpandProperty?.toggle()
            }
        }
        group {
            button(
                MaterialIcon.CHEVRON_RIGHT,
                enabledProperty = traverserProperty.nullableFlatMapBinding { it?.isNextEnabled }.mapBinding { it ?: false }
            ) {
                traverserProperty.value?.clickNextTrail()
            }
        }
    }

    private val logger = Logger(this)

    val entryList = traverserProperty.mapBinding { it?.entryList ?: observableListOf()}
    val characteristicList = traverserProperty.mapBinding { it?.characteristicList ?: observableListOf() }

    val traverserRenderStateProperty = traverserProperty
        .nullableFlatMapBinding { it?.renderState }

    fun traverse() {
        val planet = planetEntry.planetFile.planet

        logger.info { "Starting new traversal of '${planet.name}'" }
        try {
            traverserProperty.value = TraverserBarController(DefaultTraverser(planet, true))
            // .filter { it.status != ITraverserState.Status.Running }
            // .forEach {
            //     logger.info { it.getTrail() }
            // }
        } catch (e: Exception) {
            logger.error { e }
        }
    }

    init {
        traverserRenderStateProperty.onChange {
            val state = traverserRenderStateProperty.value

            drawable.importRobot(state?.robotDrawable)
            drawable.importServerPlanet(
                state?.planet?.importSenderGroups(
                    planetEntry.planetFile.planet, state.trail.locations
                )?.generateSenderGroupings() ?: Planet.EMPTY
            )
        }
    }


}
