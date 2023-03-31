package spaces.components

import csstype.ClassName
import csstype.MaxWidth
import csstype.PropertyName.Companion.color
import csstype.PropertyName.Companion.maxWidth
import csstype.px
import emotion.react.css
import mui.material.*

import mui.material.styles.TypographyVariant.Companion.body2
import mui.material.styles.TypographyVariant.Companion.h5
import mui.system.sx
import mui.material.Grid
import mui.system.ResponsiveStyleValue
import mui.system.responsive
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import spaces.models.SpaceModel
import spaces.xs

external interface SpaceListProps : Props {
    var spaceModel: SpaceModel
}

val SpaceList = FC<SpaceListProps> { props ->
    Grid {
        container = true
        spacing = responsive(2)

        props.spaceModel.spaces.map { space ->
            Grid {
                item = true
                xs = 12

                SpaceItem {
                    title = space.title
                    address = space.address
                }
            }
        }
    }
}

external interface SpaceItemProps : Props {
    var title: String
    var address: String
}

val SpaceItem = FC<SpaceItemProps> { props ->
    Card {
        CardActionArea {
            CardContent {
                Typography {
                    gutterBottom
                    variant = h5
                    component = div
                    +props.title
                }
                Typography {
                    variant = body2
                    +props.address
                }
            }
        }
    }
}

