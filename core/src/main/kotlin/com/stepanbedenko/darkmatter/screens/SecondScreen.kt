package com.stepanbedenko.darkmatter.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.stepanbedenko.darkmatter.DarkMatter
import ktx.log.Logger
import ktx.log.logger

private val LOG: Logger = logger<SecondScreen>()

class SecondScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        LOG.debug{"Second screen is shown"}
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)){
            game.setScreen<GameScreen>()
        }
    }
}