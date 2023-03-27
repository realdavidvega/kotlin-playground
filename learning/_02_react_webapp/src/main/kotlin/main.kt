
import emotion.react.css
import kotlinx.browser.document
import org.w3c.dom.Element
import react.Fragment
import react.ReactNode
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import video.videoListElement
import video.videoPlayerElement
import video.videoPlayerElementStyles

fun container(): Element =
    document.getElementById("root") ?: error("Couldn't find root container!")

fun main() {
    val container = container()
    createRoot(container).render(app())
}

fun app(): ReactNode = Fragment.create {
    h1 { +"KotlinConf Explorer" }
    div {
        videoListElement()
    }
    div {
        css { videoPlayerElementStyles() }
        div { videoPlayerElement() }
    }
}
