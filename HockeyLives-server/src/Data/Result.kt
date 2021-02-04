package com.example.Data

/**
 * results.kt
 *
 * This class Creates the bets result Object.
 * @property sum The sum of the bets
 * @property winningSum The sum of the winnigs
 * @property res the game results
 * @constructor creat an empty result
 */

data class Result(val sum : Double, val winningSum : Double, val res : Int)