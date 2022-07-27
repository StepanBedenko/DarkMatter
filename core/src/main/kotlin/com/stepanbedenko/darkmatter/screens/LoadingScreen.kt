package com.stepanbedenko.darkmatter.screens

import com.stepanbedenko.darkmatter.DarkMatter
import com.stepanbedenko.darkmatter.ecs.asset.SoundAsset
import com.stepanbedenko.darkmatter.ecs.asset.TextureAsset
import com.stepanbedenko.darkmatter.ecs.asset.TextureAtlasAsset
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.logger

private val LOG = logger<LoadingScreen>()

class LoadingScreen(game: DarkMatter) : DarkMatterScreen(game) {
    override fun show() {
        val old = System.currentTimeMillis()
        val assetRefs = gdxArrayOf(
            TextureAsset.values().map { assets.loadAsync(it.desriptor) },
            TextureAtlasAsset.values().map { assets.loadAsync(it.descriptor) },
            SoundAsset.values().map { assets.loadAsync(it.descriptor) }
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            LOG.debug { "Time for loading assets: ${System.currentTimeMillis()-old} ms" }
            assetsLoaded()
        }
    }

    private fun assetsLoaded() {
        game.addScreen(GameScreen(game))
        game.setScreen<GameScreen>()
        game.removeScreen<LoadingScreen>()
        dispose()
    }
}