/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import se.raneland.ffbe.state.action.GameAction

/**
 * @author Raniz
 * @since 2017-04-10.
 */
abstract class AbstractGameAction(private val repeat: Boolean = false): GameAction {

    override fun repeat() = repeat
}
