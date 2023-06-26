package spaces

import react.create
import react.dom.client.createRoot
import spaces.app.App
import web.dom.Element
import web.dom.document
import kotlinext.js.require

fun main() {
    require("./styles.css")

    val container = container()
    createRoot(container).render(App.create())
}

fun container(): Element =
    document.getElementById("root") ?: error("Couldn't find root container!")
