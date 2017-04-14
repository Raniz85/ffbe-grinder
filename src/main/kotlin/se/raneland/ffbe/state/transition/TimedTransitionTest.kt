/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.transition

import se.raneland.ffbe.state.GameState
import java.time.Duration

/**
 * A timed transition between states.
 * @author Raniz
 * @since 2017-04-10.
 */
class TimedTransitionTest(val delay: Duration): TransitionTest {
    override fun matches(gameState: GameState): Boolean {
        return delay <= gameState.timeInState
    }

    override fun toString(): String {
        return "TimedTransitionTest(delay=${delay})"
    }
}
