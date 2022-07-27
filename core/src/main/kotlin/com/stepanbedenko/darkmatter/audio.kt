package com.stepanbedenko.darkmatter

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Pool
import com.stepanbedenko.darkmatter.ecs.asset.MusicAsset
import com.stepanbedenko.darkmatter.ecs.asset.SoundAsset
import kotlinx.coroutines.launch
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.log.logger
import java.util.*
import java.util.Collections.max
import kotlin.math.max

private val LOG = logger<AudioService>()
private const val MAX_SOUND_INSTANCES = 16

interface AudioService {

    fun play(soundAsset: SoundAsset, volume: Float = 1f)
    fun play(musicAsset: MusicAsset, volume: Float = 1f, loop: Boolean = true)
    fun pause()
    fun resume()
    fun stop(clearSound: Boolean = true)
    fun update()
}

private class SoundRequest : Pool.Poolable {
    lateinit var soundAsset: SoundAsset
    var volume = 1f

    override fun reset() {
        volume = 1f
    }
}

private class SoundRequestPool : Pool<SoundRequest>(){
    override fun newObject(): SoundRequest = SoundRequest()
}

class DefaultAudioService(private val assets: AssetStorage) : AudioService {
    private val soundCache = EnumMap<SoundAsset, Sound>(SoundAsset::class.java)
    private val soundRequestPool = SoundRequestPool()
    private val soundRequests = EnumMap<SoundAsset, SoundRequest>(SoundAsset::class.java)
    private var currentMusic: Music? = null
    private var currentMusicAsset: MusicAsset? = null

    override fun play(soundAsset: SoundAsset, volume: Float) {
        when {
            soundAsset in soundRequests -> {
                soundRequests[soundAsset]?.let { request ->
                    request.volume = max(request.volume,volume)
                }
            }
            soundRequests.size >= MAX_SOUND_INSTANCES -> {
               LOG.debug { "Maximum sound request reached" }
               return
            }
            else -> {
                if(soundAsset.descriptor !in assets){
                    LOG.error {"Trying to play a sound which is not loaded: $soundAsset"}
                    return
                } else if(soundAsset !in soundCache){
                    soundCache[soundAsset] = assets[soundAsset.descriptor]
                }

                soundRequests[soundAsset] = soundRequestPool.obtain().apply {
                    this.soundAsset = soundAsset
                    this.volume = volume
                }
            }
        }
    }

    override fun play(musicAsset: MusicAsset, volume: Float, loop: Boolean) {
        val musicDeferred = assets.loadAsync(musicAsset.descriptor)
        KtxAsync.launch {
            musicDeferred.join()
            if(assets.isLoaded(musicAsset.descriptor)){
                currentMusic?.stop()
                val currentAsset = currentMusicAsset
                if(currentAsset != null){
                    assets.unload(currentAsset.descriptor)
                }
                currentMusicAsset = musicAsset
                currentMusic = assets[musicAsset.descriptor].apply {
                    this.volume = volume
                    this.isLooping = loop
                    play()
                }
            }
        }
    }

    override fun pause() {
        currentMusic?.pause()
    }

    override fun resume(){
        currentMusic?.play()
    }

    override fun stop(clearSound: Boolean) {
        currentMusic?.stop()
        if(clearSound){
            soundRequests.clear()
        }
    }

    override fun update() {
        if(!soundRequests.isEmpty()){
            soundRequests.values.forEach { request ->
                soundCache[request.soundAsset]?.play(request.volume)
                soundRequestPool.free(request)
            }
            soundRequests.clear()
        }
    }
}