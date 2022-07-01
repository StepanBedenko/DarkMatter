package com.stepanbedenko.darkmatter.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stepanbedenko.darkmatter.DarkMatter
import com.stepanbedenko.darkmatter.UNIT_SCALE
import com.stepanbedenko.darkmatter.ecs.component.*
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use
import ktx.log.Logger
import ktx.log.logger
import javax.swing.text.html.parser.Entity
import kotlin.math.min

private val LOG: Logger = logger<SecondScreen>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        LOG.debug {"Game screen is shown"}


        engine.entity {
            with<TransformComponent>{
                setInitialPosition(4.5f,8f,0f)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }

    }



    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
    }
}
