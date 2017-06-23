/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.image

import mu.KLogging
import java.awt.image.BufferedImage

const val DEFAULT_MAX_ERROR: Double = 0.09

data class ImageRegion(val image: BufferedImage, val x: Int, val y: Int) {

    companion object: KLogging()

    @JvmOverloads
    fun matches(target: BufferedImage, maxError: Double = DEFAULT_MAX_ERROR): Boolean {
        val width = image.width
        val height = image.height
        val rgb = image.getRGB(0, 0, width, height, null, 0, width)
        val targetRgb = target.getRGB(x, y, width, height, null, 0, width)
        return matches(rgb, targetRgb, maxError)
    }

    private fun matches(rgbA: IntArray, rgbB: IntArray, maxError: Double): Boolean {
        val averageError = rgbA.zip(rgbB).map { p ->
            val (a, b) = p
            val (aa, ar, ag, ab) = unpack(a)
            val (ba, br, bg, bb) = unpack(b)
            (Math.abs(ar - br) + Math.abs(ag - bg) + Math.abs(ab - bb)) / 3.0
        } .sumByDouble { it } / rgbA.size
        logger.trace("Average error: ${averageError}")
        return averageError < maxError
    }

    /**
     * Unpacks ARGB values from an integer
     */
    private fun unpack(argb: Int): ARGB {
        return ARGB(
                argb.ushr(24).and(0xFF) / 255.0,
                argb.ushr(16).and(0xFF) / 255.0,
                argb.ushr(8).and(0xFF) / 255.0,
                argb.and(0xFF) / 255.0
        )
    }
}

private data class ARGB(val alpha: Double, val red: Double, val green: Double, val blue: Double)
