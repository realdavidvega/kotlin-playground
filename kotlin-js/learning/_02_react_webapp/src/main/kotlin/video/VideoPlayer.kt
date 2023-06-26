package video

import csstype.Display
import csstype.NamedColor
import csstype.Position
import csstype.px
import emotion.react.css
import react.EmailIcon
import react.EmailShareButton
import react.FC
import react.Props
import react.ReactPlayer
import react.TelegramIcon
import react.TelegramShareButton
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3

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
            EmailShareButton {
                url = video.videoUrl
                EmailIcon {
                    size = 32
                    round = false
                }
            }
            TelegramShareButton {
                url = video.videoUrl
                TelegramIcon {
                    size = 32
                    round = false
                }
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
            ReactPlayer {
                url = video.videoUrl
                controls = true
            }
        }
    }
}
