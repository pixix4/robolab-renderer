package de.robolab.client.net

import de.robolab.client.utils.TimeoutReference
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.net.HttpMethod
import de.robolab.common.net.HttpStatusCode
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PingRobolabServer(
    val server: IRobolabServer
): IRobolabServer by server {

    private val _availableProperty = property(false)
    val availableProperty = _availableProperty.readOnly()
    val available by availableProperty

    fun update() {
        GlobalScope.launch(Dispatchers.Default) {
            _availableProperty.value = server.ping("/api")
        }
    }

    var timeoutReference: TimeoutReference? = null
    fun startPing() {
        timeoutReference?.cancel()
        timeoutReference = runAfterTimeoutInterval(1000L, this::update)
    }

    fun stopPing() {
        timeoutReference?.cancel()
        timeoutReference = null
    }

    init {
        update()
    }
}
