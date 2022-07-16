package com.stepanbedenko.darkmatter

import com.stepanbedenko.darkmatter.ecs.component.PowerUpType
import ktx.collections.GdxSet
import java.util.*
import javax.swing.text.html.parser.Entity

enum class GameEventType {
    PLAYER_DEATH,
    COLLECT_POWER_UP
}

interface GameEvent

object GameEventPlayerDeath : GameEvent {
    var distance = 0f

    override fun toString() = "GameEventPlayerDeath(distanve=$distance)"
}

object GameEventCollectPowerUp : GameEvent {
    lateinit var player: com.badlogic.ashley.core.Entity
    var type = PowerUpType.NONE

    override fun toString() = "GameEventCollectPowerUpType(player=$player, type=$type)"
}

interface  GameEventListener{
    fun onEvent(type: GameEventType, data: GameEvent? = null)
}

class GameEventManager {
    private val listeners = EnumMap<GameEventType, GdxSet<GameEventListener>>(GameEventType::class.java)

    fun addListener(type: GameEventType, listener: GameEventListener){
        var eventListeners = listeners[type]
        if(eventListeners == null){
            eventListeners = GdxSet()
            listeners[type] = eventListeners
        }
        eventListeners.add(listener)
    }

    fun removeListener(type: GameEventType, listener: GameEventListener){
        listeners[type]?.remove(listener)
    }

    fun removeListener(listener: GameEventListener){
        listeners.values.forEach { it.remove(listener) }
    }

    fun dispatchEvent(type: GameEventType, data: GameEvent? = null) {
        listeners[type]?.forEach {
            it.onEvent(type,data)
        }
    }
}