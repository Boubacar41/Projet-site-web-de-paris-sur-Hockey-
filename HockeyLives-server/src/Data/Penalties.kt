package com.example.Data

import com.example.Data.Goal.nullable
import com.example.Data.Goal.references
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Penalty : Table("penalty") {
    val id = integer("id").primaryKey().autoIncrement();
    val teamId = (integer("teamId") references  Team.id).nullable()
    val playerId = (integer("playerId") references  Player.id).nullable()
    val periodId = (integer("periodId")references  Period.id).nullable()
}

private enum class PenType {majeur,mineur}

/**
 * Penalties.kt
 *
 * This class Creates the penalties object
 * @property id The penalty ID
 * @property teamId The team id
 * @property playerId the Id of a given player
 * @property periodId the id of the needed period
 * @constructor Creates an empty penalty
 */

data class Penalties(val id: Int, val teamId: Int, val playerId : Int, val periodId: Int){
    companion object{
        @JvmStatic
        @Synchronized
        /**
         * addPenalty in a given game supplying all the needed informations
         */
        fun addPenalty(gameId: Int, teamId: Int, playerId : Int) {
            transaction{
                Penalty.insert {
                    it[Penalty.teamId] = teamId
                    it[Penalty.playerId] = playerId
                    it[Penalty.periodId] = Periods.lastPeriod(gameId)
                }
            }
        }
        @Synchronized
        @JvmStatic
        /**
         * getPenalitiesInMatch, returns all the penalties in a game for a specified team
         */
        fun getPenalitiesInMatch(idGame: Int, idTeam: Int?) : Int{
            var ret : Int = 0
            var res : Query? = null
            transaction{
                res = Period.select {
                    Period.gameId.eq(idGame)
                }
            }
            for(row in res!!){
                transaction {
                    var resu = Penalty.select {
                        Penalty.periodId.eq(row[Period.id])and Penalty.teamId.eq(idTeam)
                    }
                    for(ro in resu)
                        ret++
                }
            }
            return ret
        }
    }
}

