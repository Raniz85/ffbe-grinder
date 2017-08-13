/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import se.raneland.ffbe.service.DeviceController
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * @author Raniz
 * @since 2017-05-09.
 */
class IncrementCounterAction(val name: String, val amount: Int, repeat: Boolean): AbstractGameAction(repeat) {
    override fun execute(controller: DeviceController, counters: MutableMap<String, Int>) {
        counters.compute(name) { k, v -> (v ?: 0) + amount }
    }

    override fun toString() = "Add ${amount} to counter ${name}"
}
