import models.Speaker
import models.SpeakerId
import models.Talk

// Functional Architecture
// Dependency Injection
// We have explored how to pass dependencies down the execution tree implicitly via function receivers.
// This allows us to only specify the dependencies on the root call, and therefore avoid manual forwarding at
// every level of the execution tree, which saves lots of boilerplate. Then, on leaf nodes or when we are ready
// to perform our effects, we can access them right away since they are available in the current scope.

// This is an alternative to other traditional mechanisms for passing implicit dependencies like the Reader monad.
// You might have read about it before. The Reader does not exist in Arrow anymore since it enforces a wrapped style
// that is ultimately equivalent to Dependencies.() -> A but with more burden. That is why we have not covered it on
// this course. We don't want to promote wrapped style anymore.

//////////////////////

interface SpeakerServiceOps {
    suspend fun loadSpeakers(): List<Speaker>
}

interface TalkServiceOps {
    suspend fun loadTalks(speakerIds: List<SpeakerId>): List<Talk>
}

interface TalkDatabaseOps {
    suspend fun persistTalks(talks: List<Talk>): List<Talk>
}

class SpeakerService : SpeakerServiceOps {
    override suspend fun loadSpeakers(): List<Speaker> =
        listOf(
            Speaker("id1", "Janet", listOf()),
            Speaker("id2", "Ada", listOf())
        )
}

class TalkService : TalkServiceOps {
    override suspend fun loadTalks(speakerIds: List<SpeakerId>): List<Talk> =
        listOf(
            Talk("talk1", 45, "Script 1", "event1"),
            Talk("talk2", 40, "Script 2", "event1"),
        )
}

class TalkDatabase : TalkDatabaseOps {
    override suspend fun persistTalks(talks: List<Talk>): List<Talk> = talks // stubbed
}

suspend fun program(): List<Talk> {
    val speakerService = SpeakerService()
    val talkService = TalkService()
    val talkDatabase = TalkDatabase()
    return loadValidTalks(speakerService, talkService, talkDatabase)
}

suspend fun loadValidTalks(speakerService: SpeakerService, talkService: TalkService, talkDatabase: TalkDatabase) =
    loadAllTalks(speakerService, talkService, talkDatabase).filterNot { it.duration >= 50 || it.eventId.isBlank() }

suspend fun loadAllTalks(speakerService: SpeakerService, talkService: TalkService, talkDatabase: TalkDatabase): List<Talk> {
    val speakers = loadNetworkSpeakers(speakerService)
    val speakersWithTalks = speakers.filterNot { it.talkIds.isEmpty() }
    val networkTalks = loadNetworkTalks(talkService, speakersWithTalks.map { it.id })
    return persistTalks(talkDatabase, networkTalks)
}

suspend fun loadNetworkSpeakers(speakerService: SpeakerService): List<Speaker> =
    speakerService.loadSpeakers() // access dependencies

suspend fun loadNetworkTalks(talkService: TalkService, speakerIds: List<SpeakerId>): List<Talk> =
    talkService.loadTalks(speakerIds) // access dependencies

suspend fun persistTalks(talkDatabase: TalkDatabase, talks: List<Talk>): List<Talk> =
    talkDatabase.persistTalks(talks) // access dependencies

// Dependencies abstract class lazily provides implementations for the SpeakerServiceOps,
// TalkServiceOps, and TalkDatabaseOps algebras. The implementations to provide are also available just below the
// algebra declarations.
// We want the Dependencies to be provided lazily, so they are not instantiated as soon as the execution reaches a
// reference to the Dependencies graph, but only when they are needed.
// Move all functions receiving those dependencies as parameters to get them passed implicitly via receiver.
// That will also require a minor refactor to program function, that will need to create an instance of the
// dependency graph for calling our program on top of it.

abstract class Dependencies {
    val speakerService: SpeakerService by lazy { SpeakerService() }
    val talkService: TalkService by lazy { TalkService() }
    val talkDatabase: TalkDatabase by lazy { TalkDatabase() }
}

suspend fun programInj(): List<Talk> {
    val diScope = object : Dependencies() {}
    return diScope.loadAllTalksInj()
}

suspend fun Dependencies.loadAllTalksInj(): List<Talk> {
    val speakers = loadNetworkSpeakersInj()
    val speakersWithTalks = speakers.filterNot { it.talkIds.isEmpty() }
    val networkTalks = loadNetworkTalksInj(speakersWithTalks.map { it.id })
    return persistTalksInj(networkTalks)
}

suspend fun Dependencies.loadNetworkSpeakersInj(): List<Speaker> =
    speakerService.loadSpeakers() // access dependencies

suspend fun Dependencies.loadNetworkTalksInj(speakerIds: List<SpeakerId>): List<Talk> =
    talkService.loadTalks(speakerIds)

suspend fun Dependencies.persistTalksInj(talks: List<Talk>): List<Talk> =
    talkDatabase.persistTalks(talks)

suspend fun main() {
    val someTalks = program()
    println(someTalks)

    val talks = programInj()
    println(talks)
}
