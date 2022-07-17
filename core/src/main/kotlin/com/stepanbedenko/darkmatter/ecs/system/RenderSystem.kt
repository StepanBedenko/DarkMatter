package com.stepanbedenko.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.stepanbedenko.darkmatter.*
import com.stepanbedenko.darkmatter.ecs.component.GraphicComponent
import com.stepanbedenko.darkmatter.ecs.component.PowerUpType
import com.stepanbedenko.darkmatter.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.logger
import kotlin.math.min

private val LOG = logger<RenderSystem>()

class RenderSystem(
    private val batch: Batch,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    backgroundTexture: Texture,
    private val gameEventManager: GameEventManager
) : GameEventListener, SortedIteratingSystem(
    allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    private val background = Sprite(backgroundTexture.apply {
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    })
    private val backgroundScrollSpeed = Vector2(0.03f, -0.25f)

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.CollectPowerUp::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.CollectPowerUp::class,this)
    }

    override fun update(deltaTime: Float) {
        uiViewport.apply()
        batch.use(uiViewport.camera.combined){
            background.run {
                backgroundScrollSpeed.y = min(-0.25f,
                backgroundScrollSpeed.y + deltaTime * 1f/10f)
                scroll(backgroundScrollSpeed.x*deltaTime, backgroundScrollSpeed.y*deltaTime)
                draw(batch)
            }
        }

        forceSort()
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {

            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val transform:TransformComponent? = entity?.get(TransformComponent.mapper)
        require(transform != null){"Entity |entity| must have a TransformComponent. entity=$entity"}

        val graphic = entity[GraphicComponent.mapper]
        require(graphic!=null){ "Entity |entity| must have a GraphicComponent. entity=$entity" }

        if(graphic.sprite.texture == null){
            LOG.error{ "Entity has no texture for rendering. entity=$entity" }
            return
        }

        graphic.sprite.run {
            rotation = transform.rotationDeq
            setBounds(transform.interpolatedPosition.x,transform.interpolatedPosition.y,transform.size.x,transform.size.y)
            draw(batch)
        }
    }

    override fun onEvent(event: GameEvent){
        val powerUpEvent = event as GameEvent.CollectPowerUp

        if(powerUpEvent.type == PowerUpType.SPEED_1){
            backgroundScrollSpeed.y -= 0.25f
        }else if(powerUpEvent.type == PowerUpType.SPEED_2) {
            backgroundScrollSpeed.y -= 0.5f
        }
    }
}