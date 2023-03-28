package video

import csstype.Cursor.Companion.pointer
import emotion.react.css
import react.FC
import react.Props
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
                css {
                    cursor = pointer
                }
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
