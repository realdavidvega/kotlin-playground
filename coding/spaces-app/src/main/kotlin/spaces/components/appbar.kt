package spaces.components

import csstype.FontFamily
import csstype.number
import csstype.rem
import mui.icons.material.Rocket
import mui.material.*
import mui.material.styles.TypographyVariant.Companion.h6
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.a

val AppBar = FC<Props> {
    Box {
        sx { flexGrow = number(1.0) }

        AppBar {
            position = AppBarPosition.static

            Toolbar {
                Rocket()

                Typography {
                    variant = h6
                    component = a
                    noWrap
                    sx {
                        flexGrow = number(1.0)
                        letterSpacing = .3.rem
                        fontFamily = FontFamily.monospace
                    }
                    +"SPACES"
                }

                Button {
                    color = ButtonColor.inherit
                    +"Sign up"
                }

                Button {
                    color = ButtonColor.inherit
                    +"Login"
                }
            }
        }
    }
}
