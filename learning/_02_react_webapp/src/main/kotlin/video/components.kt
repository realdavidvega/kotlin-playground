package video

import csstype.Display
import csstype.NamedColor
import csstype.Position
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.p
import react.key

external interface VideoListProps : Props {
    var videos: List<Video>
    var selectedVideo: Video?
    var onSelectVideo: (Video) -> Unit
}

val VideoList = FC<VideoListProps> { props ->
    with(props) {
        videos.map { video ->
            p {
                key = video.id.toString()
                onClick = {
                    onSelectVideo(video)
                }
                if (video == selectedVideo) {
                    +"▶ "
                }
                +"${video.speaker}: ${video.title}"
            }
        }
    }
}

external interface VideoPlayerProps : Props {
    var video: Video
    var onWatchedButtonPressed: (Video) -> Unit
    var unwatchedVideo: Boolean
}

val VideoPlayer = FC<VideoPlayerProps> { props ->
    with(props) {
        div {
            css {
                position = Position.absolute
                top = 10.px
                right = 10.px
            }
            h3 {
                +"${video.speaker}: ${video.title}"
            }
            button {
                css {
                    display = Display.block
                    backgroundColor = if (unwatchedVideo) NamedColor.lightgreen else NamedColor.red
                }
                onClick = {
                    onWatchedButtonPressed(video)
                }
                if (unwatchedVideo) +"Mark as watched" else +"Mark as unwatched"
            }
            img {
                src = "https://via.placeholder.com/640x360.png?text=Video+Player+Placeholder"
            }
        }
    }
}
