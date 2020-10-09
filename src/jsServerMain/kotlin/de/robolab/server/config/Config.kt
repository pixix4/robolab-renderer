package de.robolab.server.config

import com.soywiz.klock.DateTime
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ID
import de.robolab.common.utils.TypedStorage
import de.robolab.server.data.IPlanetMetaStore
import de.robolab.server.data.IPlanetStore
import de.robolab.server.model.asPlanetJsonInfo

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
        val directory by item("planets.directory", "./planets/")

        //Connection string used to connect to the redis database for planet information (ID to name, locations, etc.)
        val database by item("planets.database", "redis://127.0.0.1:6379/4")
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

    //--------AUTH--------

    object Auth {
        //Base URL to use for gitlab
        val gitlabURL by item("auth.gitlabURL","https://se-gitlab.inf.tu-dresden.de")

        //Application ID of this app for requesting access-tokens from users
        val gitlabApplicationID by item("auth.gitlabApplicationID","")

        //Application Secret of this app for requesting access-tokens from  users
        val gitlabApplicationSecret by item("auth.gitlabApplicationSecret","")

        //GitLab access token to use for server operations
        val gitlabAPIToken by item("auth.gitlabAPIToken","")

        //URL the server is hosted on, including port. Required for redirects
        val hostURL by item("auth.hostURL","http://localhost:8080")

        //Algorithm to use for generating new JWTs for authentication. See https://www.npmjs.com/package/jsonwebtoken#algorithms-supported
        val tokenAlgorithm by item("auth.tokenAlgorithm","HS256")

        //Public key to use for signing JWTs
        val tokenPublicKey by item("auth.tokenPublicKey","")

        //Private key to use for verification of JWTs
        val tokenPrivateKey by item("auth.tokenPrivateKey", "")
    }
}

suspend fun IPlanetMetaStore.getSmallExamPlanetInfo(): PlanetJsonInfo =
    this.retrieveInfo(Config.Info.examPlanetSmallID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetSmallName,
        DateTime.EPOCH
    )


suspend fun IPlanetStore.getSmallExamPlanetInfo(): PlanetJsonInfo =
    this.getInfo(Config.Info.examPlanetSmallID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetSmallName,
        DateTime.EPOCH
    )

suspend fun IPlanetMetaStore.getLargeExamPlanetInfo(): PlanetJsonInfo =
    this.retrieveInfo(Config.Info.examPlanetLargeID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetLargeID),
        Config.Info.examPlanetLargeName,
        DateTime.EPOCH
    )


suspend fun IPlanetStore.getLargeExamPlanetInfo(): PlanetJsonInfo =
    this.getInfo(Config.Info.examPlanetLargeID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetLargeName,
        DateTime.EPOCH
    )
