package de.robolab.common.net.data

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.planet.ServerPlanetInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed class DirectoryInfo {

    abstract val path: String

    open val name: String
        get() = path.trimEnd('/').substringAfterLast('/')

    abstract val lastModified: DateTime
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
        constructor(path: String, lastModified: DateTime, childrenCount: Int) :
                this(path, DateFormat.FORMAT1.format(lastModified), childrenCount)

        @Transient
        private var _dateTime: DateTime? = null

        override val lastModified: DateTime
            get() {
                val dateTime0 = _dateTime

                if (dateTime0 == null) {
                    val dateTime1 = DateFormat.FORMAT1.parseUtc(lastModifiedString)
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

        constructor(path: String, lastModified: DateTime, subdirectories: List<MetaInfo>, planets: List<PlanetJsonInfo>)
                : this(path, DateFormat.FORMAT1.format(lastModified), subdirectories, planets)

        @Transient
        override val childrenCount: Int = subdirectories.size + planets.size

        @Transient
        private var _dateTime: DateTime? = null

        override val lastModified: DateTime
            get() {
                val dateTime0 = _dateTime

                if (dateTime0 == null) {
                    val dateTime1 = DateFormat.FORMAT1.parseUtc(lastModifiedString)
                    _dateTime = dateTime1
                    return dateTime1
                }

                return dateTime0
            }
    }

    data class ServerContentInfo(
        val path: String,
        val lastModified: DateTime,
        val subdirectories: List<MetaInfo>,
        val planets: List<ServerPlanetInfo>,
    )
}
