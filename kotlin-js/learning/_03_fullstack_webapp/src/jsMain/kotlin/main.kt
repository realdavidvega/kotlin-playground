import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import react.useEffectOnce
import react.useState
import shopping.ShoppingListItem
import shopping.addShoppingListItem
import shopping.deleteShoppingListItem
import shopping.getShoppingList
import shopping.inputComponent
import web.dom.document
import web.dom.Element

fun main() {

    fun foo1(s: Int): String = s.toString()
    val foo2: (Int) -> String = { it.toString() }

    val bar1 = foo1(100)
    val bar2 = foo2(100)

    val container = container()
    createRoot(container).render(App.create())
}

fun container(): Element =
    document.getElementById("root") ?: error("Couldn't find root container!")

private val scope = MainScope()

val App = FC<Props> {
    var shoppingList by useState(emptyList<ShoppingListItem>())

    useEffectOnce {
        scope.launch {
            shoppingList = getShoppingList()
        }
    }

    h1 {
        +"Full-Stack Shopping List"
    }

    ul {
        shoppingList.sortedByDescending(ShoppingListItem::priority).map { item ->
            li {
                key = item.toString()
                onClick = {
                    scope.launch {
                        deleteShoppingListItem(item)
                        shoppingList = getShoppingList()
                    }
                }
                +"[${item.priority}] ${item.desc} "
            }
        }
    }

    inputComponent {
        onSubmit = { input ->
            val cartItem = ShoppingListItem(input.replace("!", ""), input.count { it == '!' })
            scope.launch {
                addShoppingListItem(cartItem)
                shoppingList = getShoppingList()
            }
        }
    }
}
