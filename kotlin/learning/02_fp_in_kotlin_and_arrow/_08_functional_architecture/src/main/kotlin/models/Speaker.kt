package models

typealias SpeakerId = String
typealias TalkId = String
typealias EventId = String

data class Speaker(val id: SpeakerId, val name: String, val talkIds: List<TalkId>)
data class Talk(val id: TalkId, val duration: Int, val script: String, val eventId: EventId)
