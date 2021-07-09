package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move
import com.softwire.dynamite.game.Round

class MyBot1 : Bot {
    var dynamiteOwnCount = 0
    var dynamiteOpponentCount = 0
    var consecutiveDrawCount = 0
    var lastP1: Move? = null
    var lastlastP1: Move? = null
    var lastP2: Move? = null
    var lastlastP2: Move? = null

    override fun makeMove(gameState: Gamestate): Move {

        updateStats(gameState)

        var move = decideMove()

        if (move == Move.D) {
            if (dynamiteOwnCount == 100) {
                return randomMoves()
            }
            dynamiteOwnCount++
        }

        return move
    }

    fun updateStats(gameState: Gamestate) {

        fun updateConsecutiveDraw(lastP1: Move?, lastP2: Move?){
            if (lastP1 == lastP2) {
                consecutiveDrawCount++
            } else {
                consecutiveDrawCount = 0
            }
        }

        fun updateDynamiteOpponent(lastP2: Move?){
            if (lastP2 == Move.D) {
                dynamiteOpponentCount++
            }
        }

        val rounds = gameState.rounds
        val lastRound = rounds.lastIndex
        if (lastRound > 0) {
            lastlastP1 = lastP1
            lastlastP2 = lastP2
        }
        if (lastRound >= 0) {
            lastP1 = rounds[lastRound].p1
            lastP2 = rounds[lastRound].p2

            updateConsecutiveDraw(lastP1, lastP2)
            updateDynamiteOpponent(lastP2)
        }
    }

    fun randomMoves(): Move {
        val randomMoves = listOf(Move.S, Move.R, Move.P)
        val index = (0..2).shuffled().first()
        return randomMoves[index]
    }

    fun randomWithDynamite(): Move {
        val randomMoves = listOf(Move.D, Move.S, Move.R, Move.P)
        val index = (0..3).shuffled().first()
        return randomMoves[index]
    }

    fun beatPrevious(): Move {
        when(lastP2) {
            Move.D -> return Move.W
            Move.W -> return Move.P
            Move.P -> return Move.S
            Move.R -> return Move.P
            else -> return Move.R
        }
    }

    fun decideMove(): Move {

        //Opponent Depleted Dynamite
        if (dynamiteOpponentCount >= 100) {
            return Move.D
        }

        //Consecutive Draws
        if (consecutiveDrawCount > 0) {
            return Move.D
        }

        /*
        //Pattern Detection
        if (lastlastP2 == lastP2){
            return beatPrevious()
        }

         */

        //Avoid Consecutive Dynamites
        if (lastlastP1 == lastP1){
            if (lastP1 == Move.D){
                return randomMoves()
            }
        }

        return randomWithDynamite()
    }
}