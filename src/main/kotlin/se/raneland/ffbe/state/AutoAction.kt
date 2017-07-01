/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state

import se.raneland.ffbe.state.action.GameAction
import se.raneland.ffbe.state.transition.TransitionTest

/**
 * @author Raniz
 * @since 2017-06-27.
 */
data class AutoAction(val name: String, val tests: MutableList<TransitionTest> = mutableListOf(), val actions: List<GameAction> = mutableListOf())
