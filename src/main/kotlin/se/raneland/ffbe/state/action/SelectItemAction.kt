/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import mu.KLogging
import se.raneland.ffbe.service.DeviceController

/**
 * @author Raniz
 * @since 2017-04-19.
 */
class SelectItemAction(val character: Int, val item: Int, val target: String? = null) : AbstractGameAction(false) {

    companion object : KLogging() {
        const val ABILITY_ROW_HEIGHT = 0.0918
        const val MAX_PAGES_TO_SCROLL = 3
    }

    override fun execute(controller: DeviceController, counters: MutableMap<String, Int>) {
        // Enter item selection
        logger.info("Entering item selection at character${character}")
        controller.drag("character${character}", -0.25, 0.0)

        val row = item / 2
        var scroll = row - 2

        // Scroll so the ability is on the third row
        while (scroll > 0) {
            val thisScroll = Math.min(scroll, MAX_PAGES_TO_SCROLL)
            logger.info("Scrolling ${thisScroll} pages")
            controller.drag("character4", 0.0, -ABILITY_ROW_HEIGHT * thisScroll, 300 * thisScroll)
            scroll -= thisScroll
        }

        // Select the ability, if scroll is negative we will select from the first or second row, otherwise on the third
        if (item % 2 == 1) {
            // Odd
            logger.info("Tapping at character${5 + Math.min(0, scroll * 2)}")
            controller.tap("character${5 + Math.min(0, scroll * 2)}")
        } else {
            // Even
            logger.info("Tapping at character${4 + Math.min(0, scroll * 2)}")
            controller.tap("character${4 + Math.min(0, scroll * 2)}")
        }
        if (target != null) {
            logger.info("Selecting ${target} as item target")
            controller.tap(target)
        }

    }

    override fun toString() = "Select item ${item} for character ${character}"
}
