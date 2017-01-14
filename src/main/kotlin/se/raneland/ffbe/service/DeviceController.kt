/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import se.vidstige.jadb.JadbConnection
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.annotation.PreDestroy
import javax.imageio.ImageIO

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@Component
class DeviceController {

    @Value("\${ffmpeg.loctation:ffmpeg}")
    val ffmpegLocation = "ffmpeg"

    companion object : KLogging()

    val adb: JadbConnection = JadbConnection()
    val devices = adb.devices.toMutableList()

    @Volatile var collector: ScreenshotCollector? = null

    @Volatile var currentDevice = if (!devices.isEmpty()) devices[0] else null
    set(value) {
        field = value
        if (value == null) {
            stopCollecting()
        } else {
            startCollecting()
        }
    }

    @Volatile var collectScreenshots: Boolean = false
    get() {
        return field
    }
    set(value) {
        field = value
        if (value) {
            startCollecting()
        } else {
            stopCollecting()
        }
    }

    private val screenshotListeners: MutableList<(BufferedImage) -> Unit> = mutableListOf()

    @PreDestroy
    fun terminate() {
        collector?.stopAndWait()
    }

    fun addScreenshotListener(listener:(BufferedImage) -> Unit ) {
        screenshotListeners.add(listener)
    }

    fun refreshDevices() {
        devices.clear()
        devices.addAll(adb.devices)
    }

    fun tap(x: Int, y: Int) {
        input("tap", x, y)
    }

    fun tap(point: Point) {
        input("tap", point.x, point.y)
    }

    fun input(vararg args: Any) {
        val device = currentDevice ?: return
        val stringArgs = args.map { it.toString() }
        device.executeShell("input", *stringArgs.toTypedArray()).close()
    }

    fun screenshot(): BufferedImage? {
        val start = System.nanoTime()
        val device = currentDevice ?: return null
        val contents = device.executeShell("screencap", "-p").use {
            it.readBytes()
        }
        val image = ImageIO.read(ByteArrayInputStream(contents))
        logger.debug(String.format("Captured screenshot in %.3fms", (System.nanoTime() - start) / 1e6))
        return image
    }

    fun startCollecting() {
        val device = currentDevice ?: return
        val collector = this.collector
        if (collector == null || !collector.run) {
            this.collector = ScreenshotCollector(device) { image ->
                screenshotListeners.forEach { it(image) }
            }
        }
    }

    fun stopCollecting() {
        val collector = this.collector ?: return
        collector.run = false
    }
}

