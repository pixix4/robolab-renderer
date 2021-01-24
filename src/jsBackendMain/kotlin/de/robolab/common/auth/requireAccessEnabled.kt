package de.robolab.common.auth

import de.robolab.server.config.Config

actual val requireAccessEnabled: Boolean
    get() = Config.Auth.enabled
