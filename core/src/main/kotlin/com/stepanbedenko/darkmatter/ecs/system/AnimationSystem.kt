package com.stepanbedenko.darkmatter.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.GdxRuntimeException
import com.stepanbedenko.darkmatter.ecs.component.Animation2D
import com.stepanbedenko.darkmatter.ecs.component.AnimationComponent
import com.stepanbedenko.darkmatter.ecs.component.AnimationType
import com.stepanbedenko.darkmatter.ecs.component.GraphicComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger
import java.util.*

private val LOG = logger<AnimationSystem>()

class AnimationSystem(
    private val atlas: TextureAtlas
    ) : IteratingSystem(allOf(AnimationComponent::class, GraphicComponent::class).get()),EntityListener{
    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family,this)
    }

    override fun entityAdded(entity: Entity){
        entity[AnimationComponent.mapper]?.let { aniCmp ->
            aniCmp.animation = getAnimation(aniCmp.type)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    override fun entityRemoved(entity: Entity) = Unit

    private fun getAnimation(type: AnimationType): Animation2D{
        var animation = animationCache[type]
        if(animation == null){
            var regions = atlas.findRegions(type.atlasKey)
            if(regions.isEmpty){
                LOG.error { "No regions founds for ${type.atlasKey}" }
                regions = atlas.findRegions("error")
                if(regions.isEmpty) throw GdxRuntimeException("Ther is no error region in the atlas")
            } else {
                LOG.debug{ "Adding animation of type $type with ${regions.size} regions"}
            }
            animation = Animation2D(type, regions, type.playMode, type.speedRate)
            animationCache[type] = animation
        }

        return animation
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val aniCmp = entity[AnimationComponent.mapper]
        require(aniCmp != null){ "Entity |entity| must have a AnimationComponent. entity=$entity" }
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null){ "Entity |entity| must have a GraphicComponent. entity=$entity"}

        if(aniCmp.type == AnimationType.NONE) {
            LOG.error { "No type specified for animation component $aniCmp for |entity| entity. entity=$entity" }
        }

        if(aniCmp.type == aniCmp.animation.type){
            aniCmp.stateTime += deltaTime
        } else {
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.type)
        }

        val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
        graphic.setSpriteRegion(frame)
    }
}
