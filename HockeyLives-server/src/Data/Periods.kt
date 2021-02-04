package com.example.Data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Period : Table("period") {
    val id = integer("id").primaryKey().autoIncrement();
    val gameId = (integer("gameId")references  Game.id).nullable()
    val ended = (integer("ended"))
}

/**
 * Periods.kt
 *
 * This class Creates the period object
 * @property id The period ID
 * @property gameId the id of an already created game in the DataBase
 * @property ended the value to set if a period is ended or not
 * @constructor Creates an empty period
 */

data class Periods(val id: Int, val gameId: Int, val ended : Int) {
    companion object {
        /**
         * periodEnded, Ends a period
        */
        @JvmStatic
        @Synchronized
        fun periodEnded(id: Int, matchEnded: Boolean) {
            transaction {
                var periodId = lastPeriod(id) as Int
                Period.update({ Period.id eq periodId }) {
                    it[Period.ended] = 1
                }
            }
            if (!matchEnded) {
                addPeriod(id)
            }
        }
        @JvmStatic
        @Synchronized
        /**
         * addPeriod, adds a period in the DB
         */
        fun addPeriod(id: Int) {
            transaction {
                Period.insert {
                    it[Period.gameId] = id
                }
            }
        }

        @JvmStatic
        @Synchronized
        /**
         * lastPeriod,return the last period id in a game
         * @return game ID
         */
        fun lastPeriod(gameId: Int): Int {
            var ret = 0
            transaction {
                var res = Period.select {
                    Period.gameId.eq(gameId)
                }
                for(row in res){
                    if(ret < row[Period.id])
                        ret = row[Period.id]
                }
                println(ret)
            }
            return ret
        }

        fun countPeriods(id:Int): Int {
            var res : Query? = null
            var count = 0
            transaction {
                res = Period.select {
                    Period.gameId.eq(id)
                }
                for(row in res!!) {
                    count++
                    println(row[Period.id])
                }
            }
            return count;
        }
    }
}


