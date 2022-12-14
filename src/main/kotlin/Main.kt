import bjda.BJDA
import bjda.plugins.supercommand.SuperCommandModule
import bjda.plugins.ui.UIEventModule
import commands.HelpCommand
import commands.SettingsCommands
import commands.RequestCommands
import commands.ThreadCommands
import net.dv8tion.jda.api.JDABuilder
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import commands.todo.TodoCommands
import listeners.AllEvents
import java.net.URI
import java.net.URISyntaxException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

val ctx: DSLContext = DSL.using(getConnection(), SQLDialect.POSTGRES)

fun main() {
    val jda = JDABuilder.createDefault(System.getenv("TOKEN"))
        .build()
        .awaitReady()

    jda.addEventListener(AllEvents())

    BJDA.create(jda) {
        install(
            SuperCommandModule(
                TodoCommands,
                RequestCommands,
                ThreadCommands,
                SettingsCommands,
                HelpCommand
            ),
            UIEventModule(),
        )
    }
}

@Throws(URISyntaxException::class, SQLException::class)
private fun getConnection(): Connection? {
    val url = System.getenv("DATABASE_URL")

    return if (url != null) {
        val dbUri = URI(url)
        val (username, password) = dbUri.userInfo.split(":")

        val dbUrl = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}?sslmode=require"

        DriverManager.getConnection(dbUrl, username, password)
    } else {
        val username = System.getenv("user")
        val password = System.getenv("password")

        DriverManager.getConnection("jdbc:postgresql://localhost:5432/kbot", username, password)
    }
}