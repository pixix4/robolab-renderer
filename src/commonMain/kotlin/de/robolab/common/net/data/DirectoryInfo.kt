package de.robolab.common.net.data

import kotlinx.datetime.Instant
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.utils.ServerPlanetInfo
import de.robolab.common.utils.DateFormat
import de.robolab.common.utils.formatDateTime
import de.robolab.common.utils.parseDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed class DirectoryInfo {

    abstract val path: String

    open val name: String
        get() = path.trimEnd('/').substringAfterLast('/')

    abstract val lastModified: Instant
    abstract val childrenCount: Int

    fun asDirectoryRemoteMetadata() = RemoteMetadata.Directory(
        name,
        lastModified,
        childrenCount
    )

    @Serializable
    data class MetaInfo(
        override val path: String,
        @SerialName("lastModified")
        val lastModifiedString: String,
        override val childrenCount: Int,
    ) : DirectoryInfo() {
        constructor(path: String, lastModified: Instant, childrenCount: Int) :
                this(path, formatDateTime(lastModified, DateFormat.FORMAT1), childrenCount)

        @Transient
        private var _dateTime: Instant? = null

        override val lastModified: Instant
            get() {
                val dateTime0 = _dateTime

                if (dateTime0 == null) {
                    val dateTime1 = parseDateTime(lastModifiedString, DateFormat.FORMAT1)
                    _dateTime = dateTime1
                    return dateTime1
                }

                return dateTime0
            }
    }

    @Serializable
    data class ContentInfo(
        override val path: String,
        @SerialName("lastModified")
        val lastModifiedString: String,
        val subdirectories: List<MetaInfo>,
        val planets: List<PlanetJsonInfo>,
    ) : DirectoryInfo() {

        constructor(path: String, lastModified: Instant, subdirectories: List<MetaInfo>, planets: List<PlanetJsonInfo>)
                : this(path, formatDateTime(lastModified, DateFormat.FORMAT1), subdirectories, planets)

        @Transient
        override val childrenCount: Int = subdirectories.size + planets.size

        @Transient
        private var _dateTime: Instant? = null

        override val lastModified: Instant
            get() {
                val dateTime0 = _dateTime

                if (dateTime0 == null) {
                    val dateTime1 = parseDateTime(lastModifiedString, DateFormat.FORMAT1)
                    _dateTime = dateTime1
                    return dateTime1
                }

                return dateTime0
            }
    }

    data class ServerContentInfo(
        val path: String,
        val lastModified: Instant,
        val subdirectories: List<MetaInfo>,
        val planets: List<ServerPlanetInfo>,
    )
}
