/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import mu.KLogging
import se.vidstige.jadb.JadbDevice
import java.awt.image.BufferedImage
import java.io.File
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.function.Consumer
import javax.imageio.ImageIO
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.memberProperties

class ScreenshotCollector(val device: JadbDevice, val callback: (BufferedImage) -> Unit) {

    companion object: KLogging()

    val  directory: File

    val locator = ScreenshotLocator()
    val capturer = ScreenshotGenerator()

    @Volatile var run: Boolean = true

    fun stopAndWait() {
        run = false
        logger.info("Waiting for threads to finish")
        locator.join()
        capturer.join()
    }

    init {
        directory = Files.createTempDirectory("ffbe").toFile()
        locator.start()
        capturer.start()
    }

    inner class ScreenshotLocator : Thread("screenshot-collector") {

        override fun run() {
            logger.info("Looking for screenshots in {}", directory)
            var lastRead: File? = null
            while (run) {
                // List all files
                val files = (directory.listFiles()?.toList() ?: emptyList<File>())
                        .filter { file -> file.isFile && file.name.endsWith(".png") }
                        .sorted()

                if (files.size >= 2) {
                    // Load the penultimate image
                    val imageFile = files[files.size - 2]
                    if (lastRead != imageFile) {
                        val image = ImageIO.read(imageFile)
                        lastRead = imageFile
                        callback(image)

                        // Clean up
                        //files.slice(0..(files.size - 1)).forEach { it.delete() }
                    }
                }
            }
        }
    }

    inner class ScreenshotGenerator : Thread("screenshot-collector") {

        override fun run() {
            logger.info("Starting ffmpeg output to {}", directory)
            val ffmpeg = ProcessBuilder()
                    .command("ffmpeg", "-r", "60", "-i", "-", "-y", "-r", "5", "${directory}/%05d.png")
                    .start()
            val buffer = ByteArray(1024)
            try {
                logger.info("Starting screen recording")
                device.executeShell("screenrecord", "--output-format=h264", "--time-limit=180", "--size=720x1280", "-").use { videoStream ->
                    val unfilteredStream = unfilter(videoStream)
                    while (run) {
                        val read = unfilteredStream.read(buffer)
                        ffmpeg.outputStream.write(buffer, 0, read)
                    }
                }
            } finally {
                try {
                    logger.info("Cleaning out temporary image directory")
                    directory.deleteRecursively()
                } catch (e: Throwable) {
                    logger.warn("Could not remove {}", directory, e)
                }
                logger.info("Killing ffmpeg")
                ffmpeg.destroy()
            }
        }

        private fun unfilter(stream: InputStream): InputStream {
            if (stream is FilterInputStream) {
                val inField = FilterInputStream::class.memberProperties
                        .find { it.name == "in" } ?: throw Error("FilterInputStream has no 'in' field")
                inField.isAccessible = true
                return inField.get(stream) as InputStream
            }
            return stream
        }
    }
}