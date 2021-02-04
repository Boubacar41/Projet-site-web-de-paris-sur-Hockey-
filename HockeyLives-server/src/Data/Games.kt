package com.example.Data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Game : Table("game") {
    val id = integer("id").primaryKey().autoIncrement();
    val team1Id = (integer("team1Id"))
    val team2Id = (integer("team2Id"))
    val date = (date("date").nullable())
    val ended = (integer("ended"))
}
/**
 * Bets.kt
 *
 * This class Creates the Game Object and handles all the operations related.
 * @property id The game ID
 * @property team1Id  The ID of the first team
 * @property team2Id  The ID of the second team
 * @property date the date of the game
 * @property ended  the game has ended if set to 1
 * @constructor Creates an empty game
 */

data class Games(
    val id: Int,
    val team1Id: Int,
    var team1Name: String,
    val team2Id: Int,
    var team2Name: String,
    val date: String,
    var ended: Int,
    var score1: Int,
    var score2: Int,
    var nperiod: Int
) {
    companion object {
        /**
         * gameEnded, sets a game to an end
         */
        @JvmStatic
        @Synchronized
        fun gameEnded(id: Int) {
            transaction {
                Game.update({ Game.id eq id }) {
                    it[ended] = 1
                }
            }
            Periods.periodEnded(id, true)
        }
        /**
         * getGame gets the game details with a given gameid
         * @return game details
         */
        @JvmStatic
        @Synchronized fun getGame(id : Int): DetailGame? {
            var detail : DetailGame? = null

            transaction {
                var res = Game.select{
                    Game.id.eq(id)
                }

                for(row in res){
                    detail = DetailGame(Teams.getTeam(row[Game.team1Id] as Int), Teams.getTeam(row[Game.team2Id] as Int), row[Game.date].toString(),
                        Goals.getGoalsInMatch(id, row[Game.team1Id]), Goals.getGoalsInMatch(id, row[Game.team2Id]),
                        Penalties.getPenalitiesInMatch(id, row[Game.team1Id]), Penalties.getPenalitiesInMatch(id, row[Game.team2Id]), row[Game.ended]
                    )
                    }


                }
            return detail
        }


        /**
         * getBilanGame gets the game bilan with a given gameid at end of the Game
         * @return game bilan
         */
        @JvmStatic
        @Synchronized fun getBilanGame (id : Int): ArrayList<Any> {
            var result : Int
            var sumBet : Float
            var sumWin : Float
            var bilanTot = ArrayList<Any>()


            transaction {
                var res = Game.select{
                    Game.id.eq(id)
                }


                for(row in res){
                    if (row[Game.ended]== 1){

                        result= Games.getResult(row[Game.id])
                        sumBet= Bets.getSum(row[Game.id]).toFloat()
                        sumWin= Bets.getWinningSum(row[Game.id], result).toFloat()

                        bilanTot.add(BilanGame(row[Game.id],result,sumBet,sumWin))

                        var resBets = Bet.select{
                            Bet.idGame.eq(id)
                        }

                        var winPart : Float

                        for (bet in resBets){

                            if (bet[Bet.choice] == result){
                                //Compute the win part by each winner
                                winPart= (sumBet * bet[Bet.bet])/sumWin
                            }
                            else {
                                winPart = 0.0F
                            }

                            bilanTot.add(UserBet(bet[Bet.username],bet[Bet.choice],bet[Bet.bet],winPart))
                        }

                    }

                }
            }
            return bilanTot
        }


        /**
         * getResult gets the status of a game if team1 or team2 is wining
         * @return 0 if the game is tied, 1 if team1 is winning, 2 otherwise
         */
        @JvmStatic
        @Synchronized fun getResult(gameId: Int): Int {
            var team1Goals : Int = 0
            var team2Goals : Int = 0
            transaction {
                var res = Game.select{
                    Game.id.eq(gameId)
                }
                for(row in res){
                    team1Goals = Goals.getGoalsInMatch(gameId, row[Game.team1Id])
                    team2Goals = Goals.getGoalsInMatch(gameId, row[Game.team2Id])
                }
            }
            if(team1Goals == team2Goals)
                return 0
            if(team1Goals > team2Goals)
                return 1
            return 2
        }
    }

}

data class DetailGame(val team1Name: String, val team2Name: String, val date: String, val team1Goals : Int, val team2Goals: Int, val team1Penalties : Int, val team2Penalties: Int, val isEnded : Int){
    companion object{

    }
}

data class BilanGame(val gameId: Int, val resFinal: Int, val sumTotBet: Float, val sumTotWin: Float){
    companion object{

    }
}

data class UserBet (val username:String, val choice: Int, val bet: Float, val sumWin: Float){
    companion object{

    }
}



