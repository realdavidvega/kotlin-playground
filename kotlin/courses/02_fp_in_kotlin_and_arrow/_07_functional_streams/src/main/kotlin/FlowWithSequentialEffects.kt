import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import models.*
import java.util.*

// Functional Streams
// Evaluating effects
// Flow can evaluate effects of any nature. We can make good use of the Arrow Fx operators,
// along with any of the Arrow computational blocks like the one for either suspended effects.

// Here is a little refresher for the syntax of an either suspend computation block:

suspend fun sendTweet(tweet: Tweet): Either<Throwable, Unit> =
    either {
        val userToken = requestUserToken().bind()
        val post = createPost(userToken, tweet).bind()
        sendPost(post).bind()
    }

//////////////////////

fun loadSpeaker(id: SpeakerId): Either<Error.SpeakerNotFound, SpeakerTalkIds> =
    SpeakerTalkIds(id, "Speaker $id", listOf("talk1", "talk2")).right()

fun loadSpeakerTalks(ids: List<TalkId>): Either<Error.InvalidSpeakerId, List<Talk>> =
    listOf(
        Talk("talk1", 45, "Some talk script", "1"),
        Talk("talk2", 50, "Another talk script", "2")
    ).right()

fun loadEvents(ids: List<EventId>): Either<Error.InvalidEventId, List<SpeakerEvent>> =
    ids.map { SpeakerEvent("2021-06-20", "Event $it", Date()) }.right()

// Builds a flow from an either suspend computation block.
// The Either {} must:
// loadSpeaker by the provided speakerId.
// loadSpeakerTalks with the talkIds from the loaded Speaker.
// map the loaded Talks to their eventIds and then use that list to call loadEvents.
// map the loaded Events to their names.
// Finally, emit the result of the block (names) as an Either.Right.
suspend fun eventNamesFlow(speakerId: SpeakerId): Flow<Either<Error, List<String>>> =
    flow {
        val eventNames: Either<Error, List<String>> = either {
            val speaker = loadSpeaker(speakerId).bind()
            val talks = loadSpeakerTalks(speaker.talkIds).bind()
            val events = loadEvents(talks.map { it.eventId }).bind()
            events.map { it.name }
        }
        emit(eventNames)
    }
