package de.robolab.client.ui.view.boxes

import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.robolab.client.app.model.room.RoomPlanetDocument
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.westermann.kobserve.list.sync
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class InfoBarRoomRobotsView(private val contentInfo: InfoBarRoomRobots) : View() {

    private val groupList = de.westermann.kobserve.list.observableListOf<RoomPlanetDocument.GroupState>()

    override val root = vbox {
        vgrow = Priority.ALWAYS

        listview(groupList.toFx()) {
            vgrow = Priority.ALWAYS

            setCellFactory {
                SmartListCell()
            }

            cellFormat { provider ->
                graphic = vbox {
                    addClass(MainStyle.listCellGraphic)

                    hbox {
                        style {
                            padding = box(0.4.em, 0.5.em)
                        }

                        vbox {
                            label(provider.attempt.groupName) {
                                style {
                                    fontWeight = FontWeight.BOLD
                                }
                            }
                            label(provider.description()) {
                                style {
                                    fontSize = 0.8.em
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        groupList.addAll(contentInfo.groupStateList.value)

        contentInfo.groupStateList.onChange {
            groupList.sync(contentInfo.groupStateList.value)
        }
    }
}
