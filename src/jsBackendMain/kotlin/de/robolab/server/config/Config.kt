package de.robolab.server.config

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ID
import de.robolab.common.utils.Logger
import de.robolab.common.utils.TypedStorage
import de.robolab.server.data.IPlanetMetaStore
import de.robolab.server.data.IPlanetStore
import de.robolab.server.model.asPlanetJsonInfo
import kotlinx.datetime.Instant

object Config : TypedStorage() {

    object General {
        val port by item("general.port", 8080)
        val logLevel by item("general.logLevel", Logger.Level.INFO)
    }

    object Api {
        val mount by item("api.mount", "/api")
    }

    object Web {
        val mount by item("web.mount", "")
        val directory by item("web.directory", "")
    }

    object Electron {
        val mount by item("electron.mount", "")
        val directory by item("electron.directory", "")
    }

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

        //Regex to determine which subdirectories of `planets.directory` can be viewed. This only affects direct children.
        val firstSubdirectoryWhitelistRegex by item(
            "planets.firstSubdirectoryWhitelistRegex",
            "^(?:\\d+|archive|live)?\$"
        )

        //Connection string used to connect to the redis database for planet information (ID to name, locations, etc.)
        val database by item("planets.database", "redis://127.0.0.1:6379/4")

        //Name to set using "CLIENT SETNAME" when connecting to the redis database
        val connectionName by item("planets.connectionName", "")
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

    //--------MQTT--------

    object MQTT {
        //Username for the Tutor-MQTT login
        val tutorUser by item("mqtt.tutorUser", "tutor")

        //Password for the Tutor-MQTT login
        val tutorPassword by item("mqtt.tutorPassword", "password")

        //Connection string used to connect to the postgres database for mqtt information
        val database by item("mqtt.database", "postgresql://mothership.inf.tu-dresden.de/robolab")

        //Mothership URLs for Websockets and SSL
        val mothershipURLWSS by item("mqtt.mothershipURLWSS", "wss://mothership.inf.tu-dresden.de:9002/mqtt")
        val mothershipURLSSL by item("mqtt.mothershipURLSSL", "ssl://mothership.inf.tu-dresden.de:8883")

        //Mothership URL for logs
        val mothershipURLLog by item("mqtt.mothershipURLLog", "https://mothership.inf.tu-dresden.de/logs/mqtt/latest")
    }

    //--------AUTH--------

    object Auth {
        // Set to `false` to disable authentication for development propose
        val enabled by item("auth.enabled", true)

        //Base URL to use for gitlab
        val gitlabURL by item("auth.gitlabURL", "https://se-gitlab.inf.tu-dresden.de")

        //Application ID of this app for requesting access-tokens from users
        val gitlabApplicationID by item("auth.gitlabApplicationID", "")

        //Application Secret of this app for requesting access-tokens from  users
        val gitlabApplicationSecret by item("auth.gitlabApplicationSecret", "")

        //GitLab access token to use for server operations
        val gitlabAPIToken by item("auth.gitlabAPIToken", "")

        //URL this server is hosted on, including port. Required for redirects
        val hostURL by item("auth.hostURL", "http://localhost:8080")

        //URL the server should redirect to after authorization.
        val redirectURL by item("auth.redirectURL", "/")

        //Algorithm to use for generating new JWTs for authentication. See https://www.npmjs.com/package/jsonwebtoken#algorithms-supported
        val tokenAlgorithm by item("auth.tokenAlgorithm", "HS256")

        //Public key to use for signing JWTs
        val tokenPublicKey by item("auth.tokenPublicKey", "")

        //Private key to use for verification of JWTs
        val tokenPrivateKey by item("auth.tokenPrivateKey", "")

        //Token issuer used for verification and new tokens
        val tokenIssuer by item("auth.tokenIssuer", "robolab-renderer")

        //How long the tokens should last before expiring
        val tokenExpiration by item("auth.tokenExpiration", "6h")

        //The time required to pass before the token is valid
        val tokenNotBefore by item("auth.tokenNotBefore", "5ms")

        //Id of the gitlab-group to use for determining tutors, admins etc.
        val robolabGroupID by item("auth.robolabGroupID", 9)

        //Duration for which responses from gitlab may be cached unless a related lookup fails
        val cacheDuration by item("auth.cacheDuration", "30m")

        //Duration for which responses from gitlab will be cached even if a related lookup fails
        val cacheDurationOnKeyMiss by item("auth.cacheDurationOnKeyMiss", "2m")

        //Regex to use for determining the group-number on matching projects
        val groupProjectsRegex by item("auth.groupProjectsRegex", "^group-(\\d\\d\\d)$")

        //Projects must be direct descendants of this namespace to be tested as group-projects
        val groupProjectsGroupID by item("auth.groupProjectsGroupID", 192)
    }
}

suspend fun IPlanetMetaStore.getSmallExamPlanetInfo(): PlanetJsonInfo =
    this.retrieveInfo(Config.Info.examPlanetSmallID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetSmallName,
        Instant.DISTANT_PAST
    )


suspend fun IPlanetStore.getSmallExamPlanetInfo(): PlanetJsonInfo =
    this.getInfo(Config.Info.examPlanetSmallID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetSmallName,
        Instant.DISTANT_PAST
    )

suspend fun IPlanetMetaStore.getLargeExamPlanetInfo(): PlanetJsonInfo =
    this.retrieveInfo(Config.Info.examPlanetLargeID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetLargeID),
        Config.Info.examPlanetLargeName,
        Instant.DISTANT_PAST
    )


suspend fun IPlanetStore.getLargeExamPlanetInfo(): PlanetJsonInfo =
    this.getInfo(Config.Info.examPlanetLargeID)?.asPlanetJsonInfo() ?: PlanetJsonInfo(
        ID(Config.Info.examPlanetSmallID),
        Config.Info.examPlanetLargeName,
        Instant.DISTANT_PAST
    )
