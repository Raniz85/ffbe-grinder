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
class DelayAction(val delay: Duration, repeat: Boolean): AbstractGameAction(repeat) {
    override fun execute(controller: DeviceController) {
        val ms = TimeUnit.SECONDS.toMillis(delay.seconds) + TimeUnit.NANOSECONDS.toMillis(delay.nano.toLong());
        Thread.sleep(ms)
    }

    override fun toString() = "Wait for ${delay}"
}
