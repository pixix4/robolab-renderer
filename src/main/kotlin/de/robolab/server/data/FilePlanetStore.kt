package de.robolab.server.data

import de.robolab.server.model.ID
import de.robolab.server.model.Planet

class FilePlanetStore(val directory: String) :IPlanetStore {

    override suspend fun add(planet: Planet):Boolean{
        TODO()
    }
    override suspend fun remove(planet: Planet):Boolean{
        TODO()
    }
    override suspend fun remove(id: ID): Planet?{
        TODO()
    }

    override suspend fun get(name:String):List<Planet>{
        TODO()
    }

}