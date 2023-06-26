import csstype.px
import csstype.rgb
import react.FC
import react.Props
import emotion.react.css
import react.dom.html.InputType
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.useState

external interface WelcomeProps : Props {
    var name: String
}

val Welcome = FC<WelcomeProps> { props ->
    var name by useState(props.name)
    div {
        css {
            padding = 5.px
            backgroundColor = rgb(8, 97, 22)
            color = rgb(56, 246, 137)
        }
        +"Hello, $name!"
        +" You are now a JS Kotliner!"
        +" BTW, your name backwards is '${name.lowercase().reversed()}'"
    }
    div {
        b {
            +"Input your name: "
        }
        input {
            css {
                marginTop = 5.px
                marginBottom = 5.px
                fontSize = 14.px
            }
            type = InputType.text
            value = name
            onChange = { event ->
                name = event.target.value
            }
        }
    }
    div {
        +"Here's a kitten:"
        div {
            img {
                src = "https://placekitten.com/408/287"
            }
        }
    }
    button {
        onClick = {
            name = "Tereza"
        }
        +"Change name to Tereza"
    }
}
