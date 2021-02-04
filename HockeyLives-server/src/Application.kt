package com.example

import com.example.Data.Bets
import com.example.Data.DetailGame
import com.example.Data.Games
import com.example.Data.ListGames
import com.example.ServerCom.GamesMAJ
import com.example.ServerCom.Scope
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database



fun initDB() {
    //val url = "jdbc:mysql://34.203.246.23:3306/tests?useUnicode=true&serverTimezone=UTC&user=root&password=root"
    val url = "jdbc:mysql://root:@localhost:3306/tests?useUnicode=true&serverTimezone=UTC"
    val driver = "com.mysql.cj.jdbc.Driver"
    Database.connect(url, driver)
}

fun main(args: Array<String>) = runBlocking<Unit>  {
    initDB()
    val app = Scope()

    val launch = launch{
        var gameMaJ = GamesMAJ(app)
        gameMaJ.start()
    }

    embeddedServer(Netty, 8080) {
        install(StatusPages) {
            exception<Throwable> { e ->
                call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
        }
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        install ( WebSockets )

        routing {
            //Test fonctionnement server
            get("/") {
                call.respond(Response(status = "OK"))
            }

            post("/") {
                val request = call.receive<Request>()
                call.respond(request)
            }

            //Envoie de la liste des matchs
            get("/ListMatchs") {
                call.respond(ListGames.getAllGames())
            }

            //Envoie des details d'un match
            post("/Match") {
                val gameID = call.receive<GameID>()
                println("Id:"+gameID.id)
                call.respond(Games.getGame(gameID.id.toInt()) as DetailGame)
            }

            //Reception de données sur les paris
            put("/Pari"){
                val dataPari= call.receive<DataPari>()
                val res = Bets.placeBet(dataPari.id, dataPari.choice, dataPari.amount, dataPari.user)
                if (res){
                    call.respond(Response(status = "OK"))
                }
                else{
                    call.respond(Response(status = "Imposible"))
                }
            }

            //Envoie du bilan d'un jeu terminé
            post("/Bilan") {
                val gameID = call.receive<GameID>()
                println("Id:"+gameID.id)
                call.respond(Games.getBilanGame(gameID.id.toInt()))
            }

        }
    }.start(wait = true)

}

data class Request (val id : String,
                    val quantity: Int,
                    val isTrue: Boolean
)

//recuperer gameId
data class GameID (val id : String)

//recuperer données de pari
data class DataPari ( val id: Int,
                      val choice: Int,
                      val amount: Float,
                      val user: String
)


data class Response(val status: String)