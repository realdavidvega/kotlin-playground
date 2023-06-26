package spaces.app

import csstype.ClassName
import csstype.rem
import emotion.react.css
import react.*
import react.dom.html.ReactHTML.div
import spaces.components.AppBar
import spaces.components.SpaceList
import spaces.layouts.SpacesLayout
import spaces.models.SpacesModel

val App = FC<Props> {
    var spaces by useState(SpacesModel.empty)

    AppBar {}
    div {
        css { marginTop = 1.rem }
        div {
            className = ClassName("container mx-auto")

            SpacesLayout {
                leftSide = FilterList.create()
                rightSide = SpaceList.create {
                    spaceModel = spaces
                }
            }
        }
    }
}

val FilterList = FC<Props> {
    div {
        +"Some filters"
    }
}
