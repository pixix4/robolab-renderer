package de.robolab.server.config

import de.robolab.common.utils.TypedStorage

object Config : EnvTypedStorage() {

    object Beverage {
        //URL which should be redirected to when requesting /mate
        val payPalMateURL by item("beverage.payPalMateURL", "")

        //Text to send when no URL has been specified for mate or payPalMateForceText has been set
        val payPalMateText by item(
            "beverage.payPalMateText",
            "Please forward all of your money (or just 1.50€) to Lars Westermann"
        )

        //(If true) Send payPalMateText even if payPalMateURL is defined (custom response)
        //otherwise send default redirect-text
        //
        // !! Not supported yet !!
        val payPalMateForcedText by item("beverage.payPalMateForcedText", false)
    }

    object Planets {
        //Directory to use for planet-storage
        val directory by item("planets.directory", "./planets/")

        //Connection string used to connect to the redis database for planet information (ID to name, locations, etc.)
        val database by item("planets.database", "redis://127.0.0.1:6379/4")
    }

    object Auth {
        //File used for role-configuration
        val rolesFile by item("auth.rolesFile", "./auth/roles.txt")
    }

    object MQTT {
        val brokerURL: String by item("mqtt.brokerURL", "https://mothership.inf.tu-dresden.de:8883")
        val brokerUsername: String by item("mqtt.brokerUsername", "robolab-server")
        val brokerPassword: String by item("mqtt.brokerPassword", "\$brokerPassword")
        val brokerClientID: String by item("mqtt.brokerClientID", "robolab-renderer-004")
        val oldMessagesURL: String by item(
            "mqtt.oldMessagesURL",
            "https://mothership.inf.tu-dresden.de/logs/mqtt/latest"
        )
    }

    //--------INFO--------

    object Info {
        //Is exam-mode enabled?
        val examEnabled by item("info.examEnabled", false)

        //ID of the largePlanet. MUST be non-null if examEnabled is true
        val examPlanetLargeID by item("info.examPlanetLargeID", "MORE-HUGE")

        //ID of the smallPlanet. MUST be non-null if examEnabled is true
        val examPlanetSmallID by item("info.examPlanetSmallID", "LESS-TINY")

        //Fallback-Name for smallPlanet when it could not be found via ID
        val examPlanetLargeName by item("info.examPlanetLargeName", "ExminatorBig")

        //Fallback-Name for smallPlanet when it could not be found via ID
        val examPlanetSmallName by item("info.examPlanetSmallName", "ExminatorSmall")
    }
}
