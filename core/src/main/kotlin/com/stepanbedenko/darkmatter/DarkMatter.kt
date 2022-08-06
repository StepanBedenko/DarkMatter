package com.stepanbedenko.darkmatter

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stepanbedenko.darkmatter.ecs.asset.*
import com.stepanbedenko.darkmatter.ecs.system.*
import com.stepanbedenko.darkmatter.screens.DarkMatterScreen
import com.stepanbedenko.darkmatter.screens.LoadingScreen
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.Logger
import ktx.log.logger
import ui.createSkin

const val UNIT_SCALE:Float = 1/16f
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240
const val V_HEIGHT = 16
const val V_WIDTH = 9
private val LOG: Logger = logger<DarkMatter>()

class DarkMatter : KtxGame<DarkMatterScreen>() {
    val uiViewport = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat())
    val stage: Stage by lazy {
        val result = Stage(uiViewport,batch)
        Gdx.input.inputProcessor = result
        result
    }
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val batch:  Batch by lazy { SpriteBatch() }
    val gameEventManager = GameEventManager()
    val assets: AssetStorage by lazy {
        KtxAsync.initiate()
        AssetStorage()
    }
    val audioService by lazy { DefaultAudioService(assets) }
    val preferences: Preferences by lazy { Gdx.app.getPreferences("dark-matter") }

    val engine: Engine by lazy {
        PooledEngine().apply {
        val graphicAtlas = assets[TextureAtlasAsset.GAME_GRAPHICS.descriptor]

        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(PowerUpSystem(gameEventManager, audioService))
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
        addSystem(RenderSystem(
            batch,
            gameViewport,
            uiViewport,
            assets[TextureAsset.BACKGROUND.descriptor],
            gameEventManager,
            assets[ShaderProgramAsset.OUTLINE.descriptor])
        )
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = LOG_DEBUG
        LOG.debug{ "Create game instance" }

        val assetRefs = gdxArrayOf(
            TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assets.loadAsync(it.descriptor) },
            BitmapFontAsset.values().map { assets.loadAsync(it.descriptor) }
        ).flatten()
        KtxAsync.launch {
            assetRefs.joinAll()
            createSkin(assets)
            addScreen(LoadingScreen(this@DarkMatter))
            setScreen<LoadingScreen>()
        }

    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch: ${(batch as SpriteBatch).maxSpritesInBatch}" }
        MusicAsset.values().forEach {
            LOG.debug { "Refcount $it: ${assets.getReferenceCount(it.descriptor)}" }
        }
        batch.dispose()
        assets.dispose()
        stage.dispose()
    }
}