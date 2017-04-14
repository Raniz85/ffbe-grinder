/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import se.raneland.ffbe.state.GameState
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service that ties together everything and runs the game
 * @author Raniz
 * @since 2017-04-11.
 */
class GameService(val deviceController: DeviceController) {

    val gameStateListeners = CopyOnWriteArrayList<GameStateListener>()

    init {
        deviceController.addScreenshotListener {
        }
    }

    fun addListener(listener: GameStateListener) = gameStateListeners.add(listener)

    fun removeListener(listener: GameStateListener) = gameStateListeners.remove(listener)
}

interface GameStateListener {
    fun state(serviceState: GameServiceState)
}

data class GameServiceState(val state: ServiceState, val gameState: GameState, val message: String)

enum class ServiceState {
    RUNNING,
    PAUSED,
    ERROR
}
