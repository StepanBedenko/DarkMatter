package com.stepanbedenko.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.stepanbedenko.darkmatter.*
import com.stepanbedenko.darkmatter.ecs.component.*
import ktx.ashley.*
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.logger
import kotlin.math.min

private val LOG = logger<PowerUpSystem>()
private val MAX_SPAWN_INTERVAL = 1.5f
private val MIN_SPAWN_INTERVAL = 0.9f
private val POWER_UP_SPEED = -8.75f

private class SpawnPattern(
    type1:PowerUpType = PowerUpType.NONE,
    type2:PowerUpType = PowerUpType.NONE,
    type3:PowerUpType = PowerUpType.NONE,
    type4:PowerUpType = PowerUpType.NONE,
    type5:PowerUpType = PowerUpType.NONE,
    val types: GdxArray<PowerUpType> = gdxArrayOf(type1,type2,type3,type4,type5)
)

class PowerUpSystem(
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService
) : IteratingSystem(allOf(PowerUpComponent::class,TransformComponent::class).exclude(RemoveComponent::class.java).get()) {
    private val playerRectangle = Rectangle()
    private val powerUpRectangle = Rectangle()
    private val playerEntities by lazy {
        engine.getEntitiesFor(
            allOf(PlayerComponent::class).exclude(RemoveComponent::class.java).get()
        )
    }
    private var spawnTime = 0f
    private val spawnPatterns = gdxArrayOf(
        SpawnPattern(type1 = PowerUpType.SPEED_1, type2 = PowerUpType.SPEED_2, type5 = PowerUpType.LIFE),
        SpawnPattern(type2 = PowerUpType.LIFE, type3 = PowerUpType.SHIELD , type4 = PowerUpType.SPEED_2)
    )
    private val currentSpawnPattern = GdxArray<PowerUpType>()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        spawnTime -= deltaTime
        if(spawnTime <= 0f){
            spawnTime = MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)

            if(currentSpawnPattern.isEmpty){
                currentSpawnPattern.addAll(spawnPatterns[MathUtils.random(0,spawnPatterns.size-1)].types)
                LOG.debug { "Next pattern: $currentSpawnPattern" }
            }

            val powerUpType = currentSpawnPattern.removeIndex(0)
            if(powerUpType == PowerUpType.NONE){
                return
            }

            spawnPowerUp(powerUpType, 1f * MathUtils.random(0, V_WIDTH-1), 16f)
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType, x: Float, y:Float){
        engine.entity {
            with<TransformComponent>{
                setInitialPosition(x,y,0f)
            }
            with<PowerUpComponent>{ type = powerUpType }
            with<AnimationComponent>{type = powerUpType.animationType}
            with<GraphicComponent>()
            with<MoveComponent>{speed.y = POWER_UP_SPEED
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform!=null){"Entity |entity| must have a TransformComponent. entity=$entity"}

        if(transform.position.y <= 1f){
            entity.addComponent<RemoveComponent>(engine)
            return
        }

        powerUpRectangle.set(
            transform.position.x,
            transform.position.y,
            transform.size.x,
            transform.size.y
        )

        playerEntities.forEach { player ->
            player[TransformComponent.mapper]?.let { playerTransform ->
                playerRectangle.set(
                    playerTransform.position.x,
                    playerTransform.position.y,
                    playerTransform.size.x,
                    playerTransform.size.y
                )

                if(playerRectangle.overlaps(powerUpRectangle)){
                    collectPowerUp(player, entity)
                }
            }
        }
    }

    private fun collectPowerUp(player: Entity, powerUp: Entity) {
        val powerUpComponent = powerUp[PowerUpComponent.mapper]
        require(powerUpComponent!=null){"Entity |entity| must have a PowerUpComponent. entity=$powerUp"}

        powerUpComponent.type.also { powerUpType ->
            LOG.debug{"Picking up power up of type ${powerUpComponent.type}"}

            player[MoveComponent.mapper]?.let { it.speed.y += powerUpType.speedGain }
            player[PlayerComponent.mapper]?.let {
                it.life = min(it.maxLife, it.life + powerUpType.lifeGain)
                it.shield = min(it.maxShield, it.shield + powerUpType.shieldGain)
            }

            gameEventManager.dispatchEvent(GameEvent.CollectPowerUp.apply {
                this.player = player
                this.type = powerUpType
            })

        }

        powerUp.addComponent<RemoveComponent>(engine)

    }
}