package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.event.EventHandler

interface IFilePlanetLoader<T: IFilePlanetIdentifier> {

    val onRemoteChange: EventHandler<Unit>

    suspend fun loadContent(identifier: T): Pair<T, List<String>>?

    suspend fun saveContent(identifier: T, lines: List<String>): T?

    suspend fun loadIdentifierList(): List<T>

    val name: String
    val desc: String

    val icon: MaterialIcon
}

interface IFilePlanetIdentifier {

    val name: String

    val lastModified: DateTime
}

interface IFilePlanetLoaderFactory {

    val usage: String

    val protocol: String

    fun create(uri: String): IFilePlanetLoader<*>?
}
