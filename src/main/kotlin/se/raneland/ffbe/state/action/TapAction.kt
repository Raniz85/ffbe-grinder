/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import com.fasterxml.jackson.annotation.JsonProperty
import se.raneland.ffbe.service.DeviceController
import se.raneland.ffbe.service.Point
import se.raneland.ffbe.state.action.AbstractGameAction
import java.util.Random


enum class TAP_STRATEGY {
    @JsonProperty("all") ALL,
    @JsonProperty("random") RANDOM
}

/**
 * Action that taps one or more locations on screen
 * @author Raniz
 * @since 2017-04-10.
 */
class TapAction(repeat: Boolean = false, val locations: List<String>, val strategy: TAP_STRATEGY = TAP_STRATEGY.ALL) : AbstractGameAction(repeat) {

    val random = Random()

    override fun execute(controller: DeviceController) {
        when (strategy) {
            TAP_STRATEGY.ALL -> controller.tap(*locations.toTypedArray())
            TAP_STRATEGY.RANDOM -> {
                controller.tap(locations[random.nextInt(locations.size)])
            }
        }
    }

    override fun toString(): String {
        return when (strategy) {
            TAP_STRATEGY.ALL -> "Tap at ${locations}"
            TAP_STRATEGY.RANDOM -> "Tap at one of ${locations}"
        }
    }
}
