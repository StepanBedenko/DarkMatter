package com.stepanbedenko.darkmatter.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.stepanbedenko.darkmatter.DarkMatter
import com.stepanbedenko.darkmatter.GameEventManager
import ktx.app.KtxScreen

abstract class DarkMatterScreen(
    val game:DarkMatter,
    val batch: Batch = game.batch,
    val gameViewport: Viewport = game.gameViewport,
    val uiViewport: Viewport = game.uiViewport,
    val engine: Engine = game.engine,
    val gameEventManager: GameEventManager = game.gameEventManager
    ) : KtxScreen{

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width,height,true)
        uiViewport.update(width,height,true)
    }

    }