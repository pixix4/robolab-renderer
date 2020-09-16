package de.robolab.client.app.model.room

import com.soywiz.klock.DateFormat
import com.soywiz.klock.format
import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.openSendMessageDialog
import de.robolab.client.app.repository.Attempt
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlin.math.roundToInt

class InfoBarRoomRobots(
    val groupStateList: ObservableValue<List<RoomPlanetDocument.GroupState>>,
) : IInfoBarContent {

}
