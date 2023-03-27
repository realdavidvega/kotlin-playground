
import kotlinx.browser.document
import org.w3c.dom.Element
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.useState
import video.Video
import video.VideoList
import video.VideoPlayer
import video.unwatchedList
import video.watchedList

fun container(): Element =
    document.getElementById("root") ?: error("Couldn't find root container!")

fun main() {
    val container = container()
    createRoot(container).render(app.create())
}

val app: FC<Props> = FC {
    var currentVideo: Video? by useState(null)
    var unwatchedVideos: List<Video> by useState(unwatchedList)
    var watchedVideos: List<Video> by useState(watchedList)

    h1 { +"KotlinConf Explorer" }
    h3 { +"Videos unwatched" }
    div {
        VideoList {
            videos = unwatchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
    }
    h3 { +"Videos watched" }
    div {
        VideoList {
            videos = watchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
    }
    currentVideo?.let { curr ->
        VideoPlayer {
            video = curr
            unwatchedVideo = curr in unwatchedVideos
            onWatchedButtonPressed = {
                if (video in unwatchedVideos) {
                    unwatchedVideos = unwatchedVideos - video
                    watchedVideos = watchedVideos + video
                } else {
                    watchedVideos = watchedVideos - video
                    unwatchedVideos = unwatchedVideos + video
                }
            }
        }
    }
}
