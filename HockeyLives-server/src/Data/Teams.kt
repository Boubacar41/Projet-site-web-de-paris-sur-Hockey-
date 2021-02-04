package com.example.Data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Team : Table("team") {
    val id = Team.integer("id").primaryKey().autoIncrement();
    val name = Team.varchar("name", length = 50)
}
/**
 * Teams.kt
 *
 * This class Creates the Team Object and handles all the operations related.
 * @property id The Team ID
 * @property name The team name
 */
data class Teams(val id: Int, val name: String){
    companion object{
        @JvmStatic
        @Synchronized
        /**
         * getTeam get the name of a team by ID
         * @return the name of the team
         */
        fun getTeam(id: Int) : String{
            var name : String = ""
            transaction {
                var res = Team.select {
                    Team.id.eq(id)
                }
                for (row in res){
                    name += row[Team.name]
                }
            }
            return name
        }
    }
}

