package com.example.ServerCom

import com.example.Data.Games
import com.example.Data.Goals
import com.example.Data.Penalties
import com.example.Data.Periods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.Executors

class GamesMAJ (var app : Scope) {

    val sc = Scanner(System.`in`)
    val threadPool = Executors.newFixedThreadPool(10)

    suspend fun start() = withContext(Dispatchers.Default){
        println("GAMESMAJ STARTED")
        while(true){
            println("1 - Mark game as ended")
            println("2 - Mark Period as Ended")
            println("3 - Add Goal")
            println("4 - Add Penalty")


            var sc = Scanner(System.`in`)
            var choice = sc.nextInt()

            if(choice == 1){
                endGame()
                continue
            }
            if(choice == 2){
                endPeriod()
                continue
            }
            if(choice == 3){
                addGoal()
                continue
            }
            if(choice == 4){
                addPenalty()
                continue
            }
            println("Bad choice")
        }
    }


    private fun addPenalty() {
        println("Enter the gameId teamId playerId : ")
        val gameId = sc.nextInt()
        val teamId = sc.nextInt()
        val playerId = sc.nextInt()

        Penalties.addPenalty(gameId, teamId, playerId)
        if(app.list.containsKey(gameId as Integer)){
            for(socket in app.list.get(gameId as Integer)!!){
                threadPool.execute(InformeChanges(socket, gameId, 0))
            }
        }
    }

    private fun endPeriod() {
        println("Enter the game id : ")
        val gameId = sc.nextInt()
        Periods.periodEnded(gameId, false)
        if(app.list.containsKey(gameId as Integer)){
            for(socket in app.list.get(gameId as Integer)!!){
                threadPool.execute(InformeChanges(socket, gameId, 0))
            }
        }
    }

    private fun addGoal() {
        println("Enter the gameId teamId playerId : ")
        val gameId = sc.nextInt()
        val teamId = sc.nextInt()
        val playerId = sc.nextInt()

        Goals.addGoal(gameId, teamId, playerId)
        if(app.list.containsKey(gameId as Integer)){
            for(socket in app.list.get(gameId as Integer)!!){
                threadPool.execute(InformeChanges(socket, gameId, 0))
            }
        }
    }



    private fun endGame() {
        println("Enter the game id : ")
        val gameId = sc.nextInt()
        Games.gameEnded(gameId)
        if(app.list.containsKey(gameId as Integer)){
            for(socket in app.list.get(gameId as Integer)!!){
                threadPool.execute(InformeChanges(socket, gameId, 1))
            }
        }
    }
}
