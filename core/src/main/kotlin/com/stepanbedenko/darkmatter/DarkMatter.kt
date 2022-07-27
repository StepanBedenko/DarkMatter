package com.stepanbedenko.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stepanbedenko.darkmatter.ecs.asset.TextureAsset
import com.stepanbedenko.darkmatter.ecs.asset.TextureAtlasAsset
import com.stepanbedenko.darkmatter.ecs.system.*
import com.stepanbedenko.darkmatter.screens.DarkMatterScreen
import com.stepanbedenko.darkmatter.screens.LoadingScreen
import ktx.app.KtxGame
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.log.Logger
import ktx.log.logger

const val UNIT_SCALE:Float = 1/16f
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240
const val V_HEIGHT = 16
const val V_WIDTH = 9
private val LOG: Logger = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val uiViewport = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat())
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val batch:  Batch by lazy { SpriteBatch() }
    val gameEventManager = GameEventManager()
    val assets: AssetStorage by lazy {
        KtxAsync.initiate()
        AssetStorage()
    }
    val audioService by lazy { DefaultAudioService(assets) }

    val engine: Engine by lazy { PooledEngine().apply {
        val graphicAtlas = assets[TextureAtlasAsset.GAME_GRAPHICS.descriptor]

        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(PowerUpSystem(gameEventManager))
        addSystem(DamageSystem(gameEventManager))
        addSystem(CameraShakeSystem(gameViewport.camera, gameEventManager))
        addSystem(PlayerAnimationSystem(
            graphicAtlas.findRegion("ship_base"),
            graphicAtlas.findRegion("ship_left"),
            graphicAtlas.findRegion("ship_right")
            )
        )
        addSystem(AttachSystem())
        addSystem(
            PlayerAnimationSystem(
                graphicAtlas.findRegion("ship_base"),
                graphicAtlas.findRegion("ship_left"),
                graphicAtlas.findRegion("ship_right")
        ))
        addSystem(AnimationSystem(graphicAtlas))
        addSystem(RenderSystem(batch,
            gameViewport,
            uiViewport,
            assets[TextureAsset.BACKGROUND.desriptor],
            gameEventManager)
        )
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = LOG_DEBUG
        LOG.debug{ "Create game instance" }
        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch: ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
        assets.dispose()
    }
}