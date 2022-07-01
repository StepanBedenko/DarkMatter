package com.stepanbedenko.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.stepanbedenko.darkmatter.ecs.component.PlayerComponent
import com.stepanbedenko.darkmatter.ecs.component.TransformComponent
import ktx.app.gdxError
import ktx.ashley.allOf
import ktx.ashley.get
import java.lang.Math.min

const val WINDOW_INFO_UPDATE_RATE = 0.25f

class DebugSystem : IteratingSystem(allOf(PlayerComponent::class).get()) {
    init {
        setProcessing(true)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null) {"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null) {"Entity |entity| must have a PlayerComponent. entity=$entity"}

        when{
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                transform.position.y = 1f
                player.life = 1f
                player.shield = 0f
            }
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                player.shield = min(player.maxShield, player.shield+25f)
            }
        }

        Gdx.graphics.setTitle("DM Debug - pos:${transform.position}, life:${player.life}, shield:${player.shield}")
    }
}