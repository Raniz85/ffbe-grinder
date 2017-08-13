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
class SettCounterAction(val name: String, val value: Int, repeat: Boolean): AbstractGameAction(repeat) {
    override fun execute(controller: DeviceController, counters: MutableMap<String, Int>) {
        counters.put(name, value)
    }

    override fun toString() = "Set counter ${name} to ${value}"
}
