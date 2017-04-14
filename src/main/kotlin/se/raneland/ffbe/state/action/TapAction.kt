/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import se.raneland.ffbe.service.DeviceController
import se.raneland.ffbe.service.Point
import se.raneland.ffbe.state.action.AbstractGameAction

/**
 * Action that taps one or more locations on screen
 * @author Raniz
 * @since 2017-04-10.
 */
class TapAction(repeat: Boolean = false, val locations: List<String>) : AbstractGameAction(repeat) {

    override fun execute(controller: DeviceController) {
        locations.forEach {
                controller.tap(it)
        }
    }

    override fun toString() = "Tap at ${locations}"
}
