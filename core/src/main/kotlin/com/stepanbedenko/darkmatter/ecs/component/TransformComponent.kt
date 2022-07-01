package com.stepanbedenko.darkmatter.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import ktx.ashley.get
import ktx.ashley.mapperFor

class TransformComponent : Component, Pool.Poolable, Comparable<TransformComponent> {
    val position = Vector3()
    val prevPosition = Vector3()
    val interpolatedPosition = Vector3()
    val size = Vector2(1f,1f)
    var rotationDeq = 0f

    override fun reset() {
        position.set(Vector3.Zero)
        prevPosition.set(Vector3.Zero)
        interpolatedPosition.set(Vector3.Zero)
        size.set(1f,1f)
        rotationDeq = 0f
    }

    fun setInitialPosition(x: Float, y: Float, z: Float){
        position.set(x,y,z)
        prevPosition.set(x,y,z)
        interpolatedPosition.set(x, y, z)
    }

    override fun compareTo(other: TransformComponent): Int {
        val zDiff = other.position.z.compareTo(position.z)
        return if (zDiff == 0)
            other.position.y.compareTo(position.y)
        else
            zDiff
    }

    companion object {
        val mapper = mapperFor<TransformComponent>()
    }
}

val Entity.transform: TransformComponent
    get() = this[TransformComponent.mapper]?:throw KotlinNullPointerException("There is no TransformComponent for entity $this")