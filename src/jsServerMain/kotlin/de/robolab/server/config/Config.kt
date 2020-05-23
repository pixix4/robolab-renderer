package de.robolab.server.config

object Config {

    object Beverage {
        //URL which should be redirected to when requesting /mate
        val payPalMateURL: String? = null

        //Text to send when no URL has been specified for mate or payPalMateForceText has been set
        val payPalMateText: String = "Please forward all of your money (or just 1.50€) to Lars Westerman"

        //(If true) Send payPalMateText even if payPalMateURL is defined (custom response)
        //otherwise send default redirect-text
        //
        // !! Not supported yet !!
        val payPalMateForcedText: Boolean = false
    }

    object Planets {
        //Directory to use for planet-storage
        val directory: String = "./planets/"
    }

    //--------INFO--------

    object Info {
        //Is exam-mode enabled?
        val examEnabled: Boolean = false

        //ID of the largePlanet. MUST be non-null if examEnabled is true
        val examPlanetLargeID: String? = "MORE-HUGE"

        //ID of the smallPlanet. MUST be non-null if examEnabled is true
        val examPlanetSmallID: String? = "LESS-TINY"

        //Fallback-Name for smallPlanet when it could not be found via ID
        val examPlanetLargeName: String = "ExminatorBig"

        //Fallback-Name for smallPlanet when it could not be found via ID
        val examPlanetSmallName: String = "ExminatorSmall"
    }
}