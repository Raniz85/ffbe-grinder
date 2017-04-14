/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import se.vidstige.jadb.JadbConnection
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.Pattern.matches
import javax.annotation.PreDestroy
import javax.imageio.ImageIO

val LOCATIONS = ObjectMapper(YAMLFactory()).let { mapper ->
    mapper.findAndRegisterModules()
    DeviceController::class.java.getResourceAsStream("/locations.yml").use {
        mapper.readValue<List<Locations>>(it, object : TypeReference<List<Locations>>(){})
    }
}

val SCREEN_SIZE_REGEX = Pattern.compile("Physical size: (?<width>[0-9]+)x(?<height>[0-9]+)")

data class Locations(val ratio: Double, val locations: Map<String, Point>)

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@Component
class DeviceController {

    @Value("\${ffmpeg.location:ffmpeg}")
    val ffmpegLocation = "ffmpeg"

    @Value("\${adb.location:adb}")
    val adbLocation = "adb"

    companion object : KLogging()

    val adb: JadbConnection = JadbConnection()
    val devices = adb.devices.toMutableList()

    var size = Dimension(1440, 2560)

    @Volatile var collector: ScreenshotCollector? = null

    @Volatile var currentDevice = if (!devices.isEmpty()) devices[0] else null
    set(value) {
        field = value
        if (value == null) {
            stopCollecting()
        } else {
            startCollecting()
            setLocations()
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

    private var locations: Map<String, Point> = LOCATIONS[0].locations

    private fun setLocations() {
        val info = shell("wm", "size")
        val matcher = SCREEN_SIZE_REGEX.matcher(info).takeIf(Matcher::matches) ?: error("Could not determine screen size")
        val width = matcher.group("width").toInt()
        val height = matcher.group("height").toInt()
        this.size = Dimension(width, height)
        val ratio = width.toDouble() / height.toDouble()
        try {
            this.locations = LOCATIONS.first {
                Math.abs(it.ratio - ratio) < 1e6
            }.locations
        } catch (e: NoSuchElementException) {
            error("Could not locate named locations for aspect ratio ${ratio}")
        }
    }

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

    fun tap(x: Double, y: Double) {
        val realX = (x * size.width).toInt()
        val realY = (y * size.height).toInt()
        input("tap", realX, realY)
    }

    fun tap(location: String) {
        val point = locations[location] ?: error("No location named ${location} exists")
        tap(point)
    }

    fun tap(point: Point) {
        tap(point.x, point.y)
    }

    fun input(vararg args: Any) {
        shell("input", *args)
    }

    private fun shell(command: String, vararg args: Any): String {
        val device = currentDevice ?: error("No device selected")
        val stringArgs = args.map { it.toString() }
        logger.debug("Executing '${command} ${stringArgs.joinToString(" ")}'")
        val output = device.executeShell(command, *stringArgs.toTypedArray()).use {
            it.readBytes().toString(StandardCharsets.UTF_8)
        }
        logger.debug("Output: ${output}")
        return output
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
            this.collector = ScreenshotCollector(device, adbLocation, ffmpegLocation) { image ->
                screenshotListeners.forEach { it(image) }
            }
        }
    }

    fun stopCollecting() {
        val collector = this.collector ?: return
        collector.run = false
    }
}

