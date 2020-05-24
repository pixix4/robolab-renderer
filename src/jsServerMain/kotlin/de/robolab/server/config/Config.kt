package de.robolab.server.config

import de.robolab.common.utils.TypedStorage

object Config : TypedStorage() {

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
        val directory by item("planets.directory","./planets/")
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
