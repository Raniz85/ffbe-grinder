/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.transition

import se.raneland.ffbe.state.MachineState

/**
 * @author Raniz
 * @since 2017-04-10.
 */
data class StateTransition(val test: TransitionTest, val nextState: MachineState) {
    override fun toString(): String {
        return "StateTransition(test=${test}, nextState=${nextState.name})"
    }
}
