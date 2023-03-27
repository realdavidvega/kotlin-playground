package video

import csstype.Position
import csstype.PropertiesBuilder
import csstype.px
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.img

fun ChildrenBuilder.videoListElement() {
    h3 { +"Videos to watch" }
    div {
        VideoList {
            videos = unwatchedVideos
        }
    }
    h3 { +"Videos watched" }
    div {
        VideoList {
            videos = watchedVideos
        }
    }
}

fun ChildrenBuilder.videoPlayerElement() {
    h3 { +"John Doe: Building and breaking things" }
    img { src = "https://via.placeholder.com/640x360.png?text=Video+Player+Placeholder" }
}

fun PropertiesBuilder.videoPlayerElementStyles() {
    position = Position.absolute
    top = 10.px
    right = 10.px
}
