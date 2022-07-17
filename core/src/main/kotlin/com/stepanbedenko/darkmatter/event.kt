package com.stepanbedenko.darkmatter

import com.badlogic.gdx.utils.ObjectMap
import com.stepanbedenko.darkmatter.ecs.component.PowerUpType
import ktx.collections.GdxSet
import ktx.collections.set
import java.util.*
import javax.swing.text.html.parser.Entity
import kotlin.reflect.KClass


sealed class GameEvent{
    object PlayerDeath : GameEvent() {
        var distance = 0f

        override fun toString() = "PlayerDeath(distance=$distance )"
    }
    object CollectPowerUp : GameEvent(){
        lateinit var player: Entity
        var type = PowerUpType.NONE

        override fun toString() = "CollectPowerUp(player=$player, type=$type)"
    }
}

interface  GameEventListener{
    fun onEvent(event: GameEvent)
}

class GameEventManager {
    private val listeners = ObjectMap<KClass<out GameEvent>, GdxSet<GameEventListener>>()

    fun addListener(type: KClass<out GameEvent>, listener: GameEventListener){
        var eventListeners = listeners[type]
        if(eventListeners == null){
            eventListeners = GdxSet()
            listeners.put(type, eventListeners)
        }
        eventListeners.add(listener)
    }

    fun removeListener(type: KClass<out GameEvent>, listener: GameEventListener){
        listeners[type]?.remove(listener)
    }

    fun removeListener(listener: GameEventListener){
        listeners.values().forEach { it.remove(listener) }
    }

    fun dispatchEvent(event: GameEvent) {
        listeners[event::class]?.forEach {
            it.onEvent(event)
        }
    }
}