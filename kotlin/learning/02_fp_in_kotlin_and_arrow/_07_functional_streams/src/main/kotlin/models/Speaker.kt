package models

import arrow.core.Either
import java.util.*

typealias SpeakerId = String
typealias TalkId = String
typealias EventId = String

data class SpeakerEvent(val id: EventId, val name: String, val date: Date)
data class SpeakerTalkIds(val id: SpeakerId, val name: String, val talkIds: List<TalkId>)
data class Talk(val id: TalkId, val duration: Int, val script: String, val eventId: EventId)

object TalksNotFound

fun fetchTalksFromNetwork(ids: List<TalkId>): List<Talk> = TODO()

sealed class Error {
    object SpeakerNotFound : Error()
    object InvalidSpeakerId : Error()
    object InvalidTalkId : Error()
    object InvalidEventId : Error()
    object RoomsNotFound : Error()
    object VenuesNotFound : Error()
}
