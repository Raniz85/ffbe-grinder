/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state

import se.raneland.ffbe.state.action.GameAction
import se.raneland.ffbe.state.transition.StateTransition

/**
 * @author Raniz
 * @since 2017-04-10.
 */
data class MachineState(val name: String, val transitions: MutableList<StateTransition> = mutableListOf(), val actions: List<GameAction> = mutableListOf())

