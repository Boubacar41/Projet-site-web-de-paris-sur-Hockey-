package com.example.Data

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
/**
 * ListGames.kt
 *
 * This class is a just needed for it static function getAllGames
 * returns a list of games in the dataBase
 */

class ListGames{
    companion object{
        @JvmStatic
        @Synchronized
        fun getAllGames(): ArrayList<Any> {
            val c = ArrayList<Any>()
            transaction {
                var res = Game.selectAll().limit(20)
                for (f in res) {
                    //if(f[Game.date]?.toDate()!!.day == Date.from(Instant.now()).day)
                    //c.add(Games(id = f[Game.id], team1Id = f[Game.team1Id], team2Id = f[Game.team2Id], date = f[Game.date].toString(), ended = f[Game.ended]))
                    var name1= Teams.getTeam(f[Game.team1Id])
                    var name2= Teams.getTeam(f[Game.team2Id])
                    var scr1= Goals.getGoalsInMatch(f[Game.id], f[Game.team1Id])
                    var scr2= Goals.getGoalsInMatch(f[Game.id], f[Game.team2Id])
                    var period= Periods.countPeriods(f[Game.id])
                    c.add(Games(id = f[Game.id], team1Id = f[Game.team1Id], team1Name =name1 , team2Id = f[Game.team2Id], team2Name = name2, date = f[Game.date].toString(), ended = f[Game.ended],
                        score1 = scr1,score2 = scr2, nperiod = period))
                    //c.add(Teams.getTeam(f[Game.team1Id] as Int))
                    //c.add(Teams.getTeam(f[Game.team2Id] as Int))
                }
            }
            return c
        }
    }


}