package com.example.dynamite

import com.softwire.dynamite.bot.Bot
import com.softwire.dynamite.game.Gamestate
import com.softwire.dynamite.game.Move

class MyBot : Bot {
    override fun makeMove(gamestate: Gamestate): Move {
        // Are you debugging?
        // Put a breakpoint in this method to see when we make a move
        return Move.R
    }

    init {
        // Are you debugging?
        // Put a breakpoint on the line below to see when we start a new match
        println("Started new match")
    }
}