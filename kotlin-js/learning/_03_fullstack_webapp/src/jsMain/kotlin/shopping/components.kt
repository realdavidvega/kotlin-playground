package shopping

import react.FC
import react.Props
import react.dom.events.ChangeEventHandler
import react.dom.events.FormEventHandler
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input
import react.useState
import web.html.HTMLFormElement
import web.html.HTMLInputElement
import web.html.InputType

external interface InputProps : Props {
    var onSubmit: (String) -> Unit
}

val inputComponent = FC<InputProps> { props ->
    val (text, setText) = useState("")

    val submitHandler: FormEventHandler<HTMLFormElement> = { event ->
        event.preventDefault()
        setText("")
        props.onSubmit(text)
    }

    val changeHandler: ChangeEventHandler<HTMLInputElement> = { event ->
        setText(event.target.value)
    }

    form {
        onSubmit = submitHandler
        input {
            type = InputType.text
            onChange = changeHandler
            value = text
        }
    }
}
