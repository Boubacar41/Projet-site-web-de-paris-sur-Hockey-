package com.example.Data

import com.example.Data.Team
import org.jetbrains.exposed.sql.Table

object Player : Table("player") {
    val id = Player.integer("id").primaryKey().autoIncrement();
    val name = Player.varchar("name", length = 50)
    val idTeam = (Player.integer("idTeam")references Team.id).nullable()
}
/**
 * Players.kt
 *
 * This class Creates the player Object used just for DB.
 * @property id The player ID
 * @property name The player name
 * @property teamId team where the player belongs
 * @constructor Creates an empty player
 */
data class Players(val id: Int, val name: String, val teamId : Int)
