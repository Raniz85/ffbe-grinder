/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state

import se.raneland.ffbe.state.action.GameAction
import java.awt.image.BufferedImage
import java.time.Duration

/**
 * @author Raniz
 * @since 2017-04-10.
 */
data class GameState(val screen: BufferedImage, val timeInState: Duration, val actionsRemaining: List<GameAction>)
