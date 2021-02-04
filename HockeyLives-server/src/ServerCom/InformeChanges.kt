package com.example.ServerCom

import com.example.Data.Bets
import com.example.Data.Games
import com.example.Data.Result
import com.google.gson.GsonBuilder
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.PrintWriter
import java.net.Socket

class InformeChanges(socket: Socket, gameId: Integer, gameEnded: Int): Runnable{
    var socket = socket
    var gameId = gameId
    var gameEnded = gameEnded
    override fun run() {
        var `is` = DataInputStream(socket.getInputStream())
        var os = DataOutputStream(socket.getOutputStream())

        val gson = GsonBuilder().setPrettyPrinting().create()
        val str = gson.toJson(Games.getGame(gameId as Int)) as String

        val pw = PrintWriter(os)

        pw.println(str)
        pw.flush()

        if((gameEnded as Int) == 1){
            var gameRes = Games.getResult(gameId as Int)

            val str = gson.toJson(Result(Bets.getSum(gameId as Int), Bets.getWinningSum(gameId as Int, gameRes), gameRes)) as String

            val pw = PrintWriter(os)

            pw.println(str)
            pw.flush()
        }
    }

}