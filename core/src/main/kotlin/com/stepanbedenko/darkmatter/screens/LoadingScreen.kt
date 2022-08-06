package com.stepanbedenko.darkmatter.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.stepanbedenko.darkmatter.DarkMatter
import com.stepanbedenko.darkmatter.ecs.asset.ShaderProgramAsset
import com.stepanbedenko.darkmatter.ecs.asset.SoundAsset
import com.stepanbedenko.darkmatter.ecs.asset.TextureAsset
import com.stepanbedenko.darkmatter.ecs.asset.TextureAtlasAsset
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.logger
import ktx.scene2d.*
import ui.LabelStyles

private val LOG = logger<LoadingScreen>()

class LoadingScreen(game: DarkMatter) : DarkMatterScreen(game) {
    private lateinit var progressBar: Image
    private lateinit var touchToBeginLabel: Label

    override fun show() {
        val old = System.currentTimeMillis()
        val assetRefs = gdxArrayOf(
            TextureAsset.values().map { assets.loadAsync(it.descriptor) },
            TextureAtlasAsset.values().map { assets.loadAsync(it.descriptor) },
            SoundAsset.values().map { assets.loadAsync(it.descriptor) },
            ShaderProgramAsset.values().map { assets.loadAsync(it.descriptor)}
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            LOG.debug { "Time for loading assets: ${System.currentTimeMillis()-old} ms" }
            assetsLoaded()
        }

        setupUI()
    }

    override fun hide() {
        stage.clear()
    }

    private fun setupUI(){
        stage.actors {
            table {
                defaults().fillX().expand()

                label("Loading Screen", LabelStyles.GRADIENT.name){
                    wrap = true
                    setAlignment(Align.center)
                }
                row()

                touchToBeginLabel = label("Touch To Begin", LabelStyles.DEFAULT.name){
                    setWrap(true)
                    setAlignment(Align.center)
                    color.a = 0f
                }
                row()

                stack { cell ->
                    progressBar = image("life_bar").apply {
                        scaleX = 0f
                    }
                    label("Loading...", LabelStyles.DEFAULT.name){
                        setAlignment(Align.center)
                    }
                    cell.padLeft(5f).padRight(5f)
                }

                setFillParent(true)
                pack()
            }
        }
        stage.isDebugAll = true
    }

    override fun render(delta: Float) {
        if(assets.progress.isFinished && Gdx.input.justTouched() && game.containsScreen<GameScreen>()){
            game.setScreen<GameScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()
        }

        progressBar.scaleX = assets.progress.percent
        stage.run {
            viewport.apply(){
                act()
                draw()
            }
        }
    }

    private fun assetsLoaded() {
        game.addScreen(GameScreen(game))
        touchToBeginLabel += forever(sequence(fadeIn(0.5f) + fadeOut(0.5f)))
    }
}