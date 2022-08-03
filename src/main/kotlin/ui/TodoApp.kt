package ui

import bjda.plugins.ui.hook.ButtonClick.Companion.onClick
import bjda.plugins.ui.hook.MenuSelect.Companion.onSelect
import bjda.plugins.ui.modal.Form.Companion.form
import bjda.plugins.ui.modal.value
import bjda.ui.component.Text
import bjda.ui.component.TextType
import bjda.ui.component.action.Button
import bjda.ui.component.action.Menu
import bjda.ui.component.action.Menu.Companion.createOptions
import bjda.ui.component.action.TextField
import bjda.ui.component.row.Row
import bjda.ui.component.row.RowLayout
import bjda.ui.core.Component
import bjda.ui.core.IProps
import bjda.ui.core.rangeTo
import bjda.ui.types.Children
import bjda.utils.Translation
import commands.todo.todoStore
import database.saveTodos
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class TodoApp(initialTodos: MutableList<String>, val lang: Translation) : Component<TodoApp.Props>(Props()) {
    class Props : IProps() {
        lateinit var owner: User
    }

    private val state = useState(
        State(
            todos = initialTodos
        )
    )

    data class State(
        val todos: MutableList<String>,
        var selected: Int? = null
    )

    private val onAddItem by onClick { event ->
        event.replyModal(addTodoForm).queue()
    }

    private val onEditItem by onClick { event ->
        event.replyModal(editTodoForm).queue()
    }

    private val onDeleteItem by onClick { event ->
        state.update(event) {
            todos.removeAt(selected!!)

            selected = null
        }
    }

    override fun onUnmount() {
        val owner = props.owner
        val (todos) = state.get()

        saveTodos(owner.idLong, todos.toTypedArray())
    }

    private val onClose by onClick { event ->
        event.deferEdit().queue {
            todoStore.invalidate(props.owner)
        }
    }

    private val onSelectItem by onSelect { event ->
        state.update(event) {
            selected = event.selectedOptions[0].value.toInt()
        }
    }

    override fun onRender(): Children {
        val (todos, selected) = state.get()

        return {
            + Text()..{
                content = "**${ lang["title"] }**"
                type = TextType.LINE
            }

            + on (todos.isEmpty()) {
                Text()..{
                    content = lang["placeholder"]
                    type = TextType.CODE_BLOCK
                }
            }

            + todos.mapIndexed {i, todo ->
                Text()..{
                    this.key = i
                    this.content = todo
                    type = TextType.CODE_BLOCK
                }
            }

            + RowLayout {
                if (todos.isNotEmpty()) {
                    + Menu(onSelectItem) {
                        placeholder = lang("menu")["placeholder"]

                        options = todos.mapIndexed {i, todo ->
                            SelectOption.of(todo, i.toString()).withDefault(i == selected)
                        }
                    }
                }

                + Button(onAddItem) {
                    label = lang["add"]
                }

                if (selected != null) {
                    + Button(onEditItem) {
                        label = lang["edit"]
                        style = ButtonStyle.PRIMARY
                    }

                    + Button(onDeleteItem) {
                        label = lang["delete"]
                        style = ButtonStyle.DANGER
                    }
                }
            }

            + Row {
                + Button(onClose) {
                    label = lang["close"]
                    style = ButtonStyle.DANGER
                }
            }
        }
    }

    private val addTodoForm by form {
        title = lang["add"]

        onSubmit = {event ->
            state.update(event) {
                todos += event.value("todo")
            }
        }

        render = {
            + Row {
                + TextField("todo") {
                    label = lang["todo"]
                    style = TextInputStyle.PARAGRAPH
                }
            }
        }
    }

    private val editTodoForm by form {
        title = lang["edit"]

        onSubmit = {event ->
            val value = event.getValue("todo")!!.asString

            state.update(event) {
                todos[selected!!] = value
            }
        }

        render = {
            val (todos, selected) = state.get()

            + Row {
                + TextField("todo") {
                    label = lang("form")["new_content"]
                    value = todos[selected!!]
                    style = TextInputStyle.PARAGRAPH
                }
            }
        }
    }
}