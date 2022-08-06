package com.stepanbedenko.darkmatter.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stepanbedenko.darkmatter.*
import com.stepanbedenko.darkmatter.ecs.asset.MusicAsset
import com.stepanbedenko.darkmatter.ecs.component.*
import com.stepanbedenko.darkmatter.ecs.system.AttachSystem
import com.stepanbedenko.darkmatter.ecs.system.DAMAGE_AREA_HEIGHT
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use
import ktx.log.Logger
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import javax.swing.text.html.parser.Entity
import kotlin.math.min
import kotlin.reflect.KClass

private val LOG: Logger = logger<GameScreen>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: DarkMatter,
    private val engine: Engine = game.engine
    ) : DarkMatterScreen(game),GameEventListener {
    override fun show() {
        LOG.debug {"Game screen is shown"}
        LOG.debug { "${preferences["highscore", 0f]}" }
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)

        audioService.play(MusicAsset.GAME)
        spawnPlayer()

    }

    override fun hide() {
        super.hide()
        gameEventManager.removeListener(this)
    }

    private fun spawnPlayer() {
        val playerShip = engine.entity {
            with<TransformComponent> {
                setInitialPosition(4.5f, 8f, -1f)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }

        engine.entity {
            with<TransformComponent>()
            with<AttachComponent> {
                entity = playerShip
                offset.set(0f * UNIT_SCALE, -12f * UNIT_SCALE)
            }
            with<GraphicComponent>()
            with<AnimationComponent> {
                type = AnimationType.FIRE
            }
        }

        engine.entity {
            with<TransformComponent> {
                size.set(
                    V_WIDTH.toFloat(),
                    DAMAGE_AREA_HEIGHT
                )
            }
            with<AnimationComponent> { type = AnimationType.DARK_MATTER }
            with<GraphicComponent>()
        }
    }


    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
        audioService.update()
    }

    override fun onEvent(event: GameEvent) {
        when(event){
            is GameEvent.PlayerDeath -> {
                LOG.debug { "Player died with a distance of ${event.distance}" }
                preferences.flush {
                    this["highscore"] = event.distance
                }
                spawnPlayer()
            }
            GameEvent.CollectPowerUp -> TODO()
            else -> {

            }
        }
    }
}
