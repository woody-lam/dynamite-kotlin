package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move

class MyBot : Bot {

    var dynamiteOwnCount = 0
    var pastData = Array(625) {IntArray(5)}

    var accuracyPrevTwoP1P2 = 1
    var accuracyPrevTwoP1 = 1
    var accuracyPrevTwoP2 = 1
    var accuracyPrevP1P2 = 1
    var accuracyPrevP1 = 1
    var accuracyPrevP2 = 1
    var lastPredictionPrevTwoP1P2: Int? = null
    var lastPredictionPrevTwoP1: Int? = null
    var lastPredictionPrevTwoP2: Int? = null
    var lastPredictionPrevP1P2: Int? = null
    var lastPredictionPrevP1: Int? = null
    var lastPredictionPrevP2: Int? = null

    var pastDataByOutcome = Array(9) {IntArray(5)}
    var accuracyByPrevTwoOutcome = 1
    var accuracyByPrevOutcome = 1
    var lastPredictionByPrevTwoOutcome: Int? = null
    var lastPredictionByPrevOutcome: Int? = null


    fun numberFromMove(move: Move): Int {
        return when(move){
            Move.P -> 0
            Move.S -> 1
            Move.R -> 2
            Move.W -> 3
            else -> 4
        }
    }

    fun moveFromNumber(number: Int): Move {
        return when(number){
            0 -> Move.P
            1 -> Move.S
            2 -> Move.R
            3 -> Move.W
            else -> Move.D
        }
    }

    fun determineRoundOutcome(p1: Move, p2: Move): Int {
        if (p1 == p2){
            return 0 //Draw
        }
        if (p1 == Move.D){
            if (p2 == Move.W){
                return 2 //Lose
            }
            return 1 //Win
        }
        if (p1 == Move.W){
            if (p2 == Move.D){
                return 1 //Win
            }
            return 2 //Lose
        }
        if (p1 == Move.P){
            if (p2 == Move.S || p2 == Move.D){
                return 2 //Lose
            }
            return 1 //Win
        }
        if (p1 == Move.S){
            if (p2 == Move.R || p2 == Move.D){
                return 2 //Lose
            }
            return 1 //Win
        }
        if (p2 == Move.P || p2 == Move.D){
            return 2 //Lose
        }
        return 1 //Win
    }

    fun numberRound(p1: Move, p2: Move): Int {
        return numberFromMove(p1) * 5 + numberFromMove(p2)
    }

    fun numberPastTwoRounds(numberPrevRound: Int, numberPrevPrevRound: Int): Int {
        return numberPrevRound + numberPrevPrevRound * 25
    }

    fun numberPastTwoRoundsOutcome(numberPrevRoundOutcome: Int, numberPrevPrevRoundOutcome: Int): Int {
        return numberPrevRoundOutcome + numberPrevPrevRoundOutcome * 3
    }

    fun updateStats(gameState: Gamestate) {

        val rounds = gameState.rounds
        val lastRound = rounds.lastIndex

        if (lastRound >= 2){
            val opponentLastMove = rounds[lastRound].p2

            if (opponentLastMove != null){
                val numberOpponentLastMove = numberFromMove(opponentLastMove)
                if(lastPredictionPrevTwoP1P2 == numberOpponentLastMove){
                    accuracyPrevTwoP1P2++
                }
                if(lastPredictionPrevTwoP1 == numberOpponentLastMove){
                    accuracyPrevTwoP1++
                }
                if(lastPredictionPrevTwoP2 == numberOpponentLastMove){
                    accuracyPrevTwoP2++
                }
                if(lastPredictionPrevP1P2 == numberOpponentLastMove){
                    accuracyPrevP1P2++
                }
                if(lastPredictionPrevP1 == numberOpponentLastMove){
                    accuracyPrevP1++
                }
                if(lastPredictionPrevP2 == numberOpponentLastMove){
                    accuracyPrevP2++
                }
                if(lastPredictionByPrevTwoOutcome == numberOpponentLastMove){
                    accuracyByPrevTwoOutcome++
                }
                if(lastPredictionByPrevOutcome == numberOpponentLastMove){
                    accuracyByPrevOutcome++
                }
            }

            val PrevPrevRound = rounds[lastRound - 1]
            val numberPrevPrevRound = numberRound(PrevPrevRound.p1, PrevPrevRound.p2)
            val PrevPrevPrevRound = rounds[lastRound - 2]
            val numberPrevPrevPrevRound = numberRound(PrevPrevPrevRound.p1, PrevPrevPrevRound.p2)
            val numberPastTwo = numberPastTwoRounds(numberPrevPrevRound, numberPrevPrevPrevRound)

            val PrevP2 = rounds[lastRound].p2
            val numberPrevP2 = numberFromMove(PrevP2)
            pastData[numberPastTwo][numberPrevP2]++

            val numberPrevPrevRoundOutcome = determineRoundOutcome(PrevPrevRound.p1, PrevPrevRound.p2)
            val numberPrevPrevPrevRoundOutcome = determineRoundOutcome(PrevPrevPrevRound.p1, PrevPrevPrevRound.p2)
            val numberPastTwoOutcome = numberPastTwoRoundsOutcome(numberPrevPrevRoundOutcome, numberPrevPrevPrevRoundOutcome)
            pastDataByOutcome[numberPastTwoOutcome][numberPrevP2]++
        }
    }

    fun predictOpponentMove(gameState: Gamestate, threshold: Float, thresholdOutcome: Float, bias: Float): Move? {
        val rounds = gameState.rounds
        val lastRound = rounds.lastIndex

        fun AddTwoArrayLengthFive(array1: IntArray, array2: IntArray): IntArray {
            return IntArray(5) {array1[it] + array2[it]}
        }

        fun statByPrevTwoP1P2 (numberPastTwo: Int): IntArray {
            return pastData[numberPastTwo]
        }

        fun statByPrevTwoP1 (numberPastTwo: Int): IntArray {
            val numberPrevP1 = (numberPastTwo % 25) / 5
            val numberPrevPrevP1 = numberPastTwo / 125
            var result = IntArray(5)

            for (i in 0..4){
                for (j in 0..4){
                    result = AddTwoArrayLengthFive(result, pastData[numberPrevPrevP1 * 125 + i * 25 + numberPrevP1 * 5 + j])
                }

            }
            return result
        }

        fun statByPrevTwoP2 (numberPastTwo: Int): IntArray {
            val numberPrevP2 = numberPastTwo % 5
            val numberPrevPrevP2 = (numberPastTwo / 25) % 5
            var result = IntArray(5)

            for (i in 0..4){
                for (j in 0..4){
                    result = AddTwoArrayLengthFive(result, pastData[i * 125 + numberPrevPrevP2 * 25 + j * 5 + numberPrevP2])
                }

            }
            return result
        }

        fun statByPrevP1P2 (numberPastTwo: Int): IntArray {
            val numberPrev = numberPastTwo % 25
            var result = IntArray(5)

            for (i in 0..24){
                result = AddTwoArrayLengthFive(result, pastData[i * 25 + numberPrev])
            }
            return result
        }

        fun statByPrevP1 (numberPastTwo: Int): IntArray {
            val numberPrevP1 = (numberPastTwo % 25) / 5
            var result = IntArray(5)

            for (i in 0..24){
                for (j in 0..4){
                    result = AddTwoArrayLengthFive(result, pastData[i * 25 + numberPrevP1 * 5 + j])
                }
            }
            return result
        }

        fun statByPrevP2 (numberPastTwo: Int): IntArray {
            val numberPrevP2 = numberPastTwo % 5
            var result = IntArray(5)

            for (i in 0..24){
                for (j in 0..4){
                    result = AddTwoArrayLengthFive(result, pastData[i * 25 + j * 5 + numberPrevP2])
                }
            }
            return result
        }

        fun statByPrevTwoOutcome (numberPastTwoOutcome: Int): IntArray {
            return pastDataByOutcome[numberPastTwoOutcome]
        }

        fun statByPrevOutcome (numberPastTwoOutcome: Int): IntArray {
            val numberPrevOutcome = numberPastTwoOutcome % 3
            var result = IntArray(5)

            for (i in 0..2){
                result = AddTwoArrayLengthFive(result, pastDataByOutcome[i * 3 + numberPrevOutcome])
            }
            return result
        }

        fun maxMoveFromStat (stat: IntArray, threshold: Float): Int?{
            val max = stat.max()

            return when {
                max == null -> null
                max.toFloat()/stat.sum() < threshold -> null
                else -> stat.indexOf(max)
            }
        }

        fun updateMoves (moves: FloatArray, prediction: Int?, accuracy: Float): FloatArray{
            if (prediction != null){
                moves[prediction] += accuracy
            }
            return moves
        }

        if (lastRound >= 3){
            val prevRound = rounds[lastRound]
            val numberPrevRound = numberRound(prevRound.p1, prevRound.p2)
            val prevPrevRound = rounds[lastRound - 1]
            val numberPrevPrevRound = numberRound(prevPrevRound.p1, prevPrevRound.p2)
            val numberPastTwo = numberPastTwoRounds(numberPrevRound, numberPrevPrevRound)

            val numberPrevRoundOutcome = determineRoundOutcome(prevRound.p1, prevRound.p2)
            val numberPrevPrevRoundOutcome = determineRoundOutcome(prevPrevRound.p1, prevPrevRound.p2)
            val numberPastTwoOutcome = numberPastTwoRoundsOutcome(numberPrevRoundOutcome, numberPrevPrevRoundOutcome)

            val moves = FloatArray(5)


            lastPredictionPrevTwoP1P2 = maxMoveFromStat(statByPrevTwoP1P2(numberPastTwo), threshold)
            lastPredictionPrevTwoP1 = maxMoveFromStat(statByPrevTwoP1(numberPastTwo), threshold)
            lastPredictionPrevTwoP2 = maxMoveFromStat(statByPrevTwoP2(numberPastTwo), threshold)
            lastPredictionPrevP1P2 = maxMoveFromStat(statByPrevP1P2(numberPastTwo), threshold)
            lastPredictionPrevP1 = maxMoveFromStat(statByPrevP1(numberPastTwo), threshold)
            lastPredictionPrevP2 = maxMoveFromStat(statByPrevP2(numberPastTwo), threshold)

            lastPredictionByPrevTwoOutcome = maxMoveFromStat(statByPrevTwoOutcome(numberPastTwoOutcome), thresholdOutcome)
            lastPredictionByPrevOutcome = maxMoveFromStat(statByPrevOutcome(numberPastTwoOutcome), thresholdOutcome)

            updateMoves(moves, lastPredictionPrevTwoP1P2, accuracyPrevTwoP1P2.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionPrevTwoP1, accuracyPrevTwoP1.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionPrevTwoP2, accuracyPrevTwoP2.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionPrevP1P2, accuracyPrevP1P2.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionPrevP1, accuracyPrevP1.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionPrevP2, accuracyPrevP2.toFloat() / lastRound + bias)

            updateMoves(moves, lastPredictionByPrevTwoOutcome, accuracyByPrevTwoOutcome.toFloat() / lastRound + bias)
            updateMoves(moves, lastPredictionByPrevOutcome, accuracyByPrevOutcome.toFloat() / lastRound + bias)

            val movesMax = moves.max()

            return when {
                movesMax == null -> null
                moves.sum() / 2 < threshold -> null
                else -> moveFromNumber(moves.indexOf(movesMax))
            }
        }

        return null
    }

    override fun makeMove(gameState: Gamestate): Move {

        updateStats(gameState)

        val opponentMove = predictOpponentMove(gameState, (0.6).toFloat(), (0.7).toFloat(), (0.1).toFloat())

        var move = decideMove(opponentMove)

        if (move == Move.D) {
            if (dynamiteOwnCount == 100) {
                return randomMoves()
            }
            dynamiteOwnCount++
        }

        return move
    }



    fun decideMove(opponentMove: Move?): Move {
        if (opponentMove != null){
            return when(opponentMove){
                Move.P -> Move.S
                Move.S -> Move.R
                Move.R -> Move.P
                Move.D -> Move.W
                else -> Move.R
            }
        }
        return randomWithDynamite()
    }

    fun randomMoves(): Move {
        val randomMoves = listOf(Move.S, Move.R, Move.P)
        val index = (0..2).shuffled().first()
        return randomMoves[index]
    }

    fun randomWithDynamite(): Move {
        val dynamite = listOf(Move.D, Move.D, Move.S, Move.R, Move.P)
        val index = (0..4).shuffled().first()
        return dynamite[index]
    }

}