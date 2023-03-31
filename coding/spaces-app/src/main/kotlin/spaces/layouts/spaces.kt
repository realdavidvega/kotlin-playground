package spaces.layouts

import csstype.ClassName
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div

external interface SpacesLayoutProps : Props {
    var leftSide: ReactNode
    var rightSide: ReactNode
}

val SpacesLayout = FC<SpacesLayoutProps> { props ->
    div {
        className = ClassName("grid grid-cols-4 gap-4")
        div {
            className = ClassName("col-span-1")
            +props.leftSide
        }
        div {
            className = ClassName("col-span-3")
            +props.rightSide
        }
    }
}
