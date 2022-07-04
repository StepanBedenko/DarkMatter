package com.stepanbedenko.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stepanbedenko.darkmatter.ecs.system.*
import com.stepanbedenko.darkmatter.screens.DarkMatterScreen
import com.stepanbedenko.darkmatter.screens.GameScreen
import com.stepanbedenko.darkmatter.screens.SecondScreen
import ktx.app.KtxGame
import ktx.log.Logger
import ktx.log.logger

const val UNIT_SCALE:Float = 1/16f
const val V_HEIGHT = 16
const val V_WIDTH = 9
private val LOG: Logger = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val batch:  Batch by lazy { SpriteBatch() }
    val graphicAtlas by lazy { TextureAtlas(Gdx.files.internal("graphics/graphics.atlas")) }

    val engine: Engine by lazy { PooledEngine().apply {
        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(DamageSystem())
        addSystem(PlayerAnimationSystem(
            graphicAtlas.findRegion("ship_base"),
            graphicAtlas.findRegion("ship_left"),
            graphicAtlas.findRegion("ship_right")
            )
        )
        addSystem(
            PlayerAnimationSystem(
                graphicAtlas.findRegion("ship_base"),
                graphicAtlas.findRegion("ship_left"),
                graphicAtlas.findRegion("ship_right")
        ))
        addSystem(AnimationSystem(graphicAtlas))
        addSystem(RenderSystem(batch,gameViewport))
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = LOG_DEBUG
        LOG.debug{ "Create game instance" }
        addScreen(GameScreen(this))
        addScreen(SecondScreen(this))
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch: ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()

        graphicAtlas.dispose()
    }
}