package com.stepanbedenko.darkmatter.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.stepanbedenko.darkmatter.DarkMatter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

    fun main() {
        Lwjgl3Application(DarkMatter(), Lwjgl3ApplicationConfiguration().apply {
            setTitle("DarkMatterGame")
            setWindowedMode(9 * 32, 16 * 32)
            setWindowIcon(
                "libgdx128.png",
                "libgdx64.png",
                "libgdx32.png",
                "libgdx16.png"
            )
        })
    }