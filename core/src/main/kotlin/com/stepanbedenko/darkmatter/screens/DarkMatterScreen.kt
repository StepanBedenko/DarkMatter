package com.stepanbedenko.darkmatter.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.stepanbedenko.darkmatter.DarkMatter
import com.stepanbedenko.darkmatter.DefaultAudioService
import com.stepanbedenko.darkmatter.GameEventManager
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import java.util.prefs.Preferences

abstract class DarkMatterScreen(
    val game:DarkMatter,
    val gameViewport: Viewport = game.gameViewport,
    val uiViewport: Viewport = game.uiViewport,
    val gameEventManager: GameEventManager = game.gameEventManager,
    val assets : AssetStorage = game.assets,
    val audioService: DefaultAudioService = game.audioService,
    val preferences: com.badlogic.gdx.Preferences = game.preferences
    ) : KtxScreen{

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width,height,true)
        uiViewport.update(width,height,true)
    }

    }