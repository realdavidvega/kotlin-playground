package naps.domain.naps

interface NapService {
    suspend fun createNap(napTime: NapTime): Long
    suspend fun getNap(id: Long): NapTime
    suspend fun deleteNap(id: Long): Boolean
}

fun napService(persistence: NapPersistence): NapService = object : NapService {
    override suspend fun createNap(napTime: NapTime): Long {
        return persistence.create(napTime)
    }

    override suspend fun getNap(id: Long): NapTime {
        return persistence.read(id)
    }

    override suspend fun deleteNap(id: Long): Boolean {
        return persistence.delete(id)
    }
}
