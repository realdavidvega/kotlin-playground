package video

import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.ReactHTML.p
import react.key

external interface VideoListProps : Props {
    var videos: List<Video>
}

val VideoList: FC<VideoListProps> = FC { props ->
    with(props) {
        videos.map { video ->
            p {
                key = video.id.toString()
                onClick = {
                    window.alert("Clicked $video!")
                }
                +"${video.speaker}: ${video.title}"
            }
        }
    }
}