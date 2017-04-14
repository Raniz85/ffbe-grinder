/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import mu.KLogging
import se.vidstige.jadb.JadbDevice
import java.awt.image.BufferedImage
import java.io.DataInputStream
import java.io.EOFException
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.IntBuffer
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ScreenshotCollector(val device: JadbDevice, val adbLocation: String, val ffmpegLocation: String,
        val callback: (BufferedImage) -> Unit) : Thread("screenshot-collector") {

    companion object: KLogging() {
        const val width = 720
        const val height = 1280
    }

    var ffmpeg : Process
    var imageStream : InputStream

    val streamLock = Any()

    @Volatile var run: Boolean = true

    init {
        ffmpeg = startFfmpeg()
        imageStream = ffmpeg.inputStream
        start()
    }

    private fun startFfmpeg() : Process {
        logger.info("Starting ffmpeg")
        val adbCommand = "${adbLocation} -s ${device.serial} shell screenrecord --output-format=h264 --time-limit=180 --size=${width}x${height} - | ${ffmpegLocation} -r 60 -i - -f image2pipe -c:v rawvideo -pix_fmt argb -r 5 -"
        //val adbCommand = "${adbLocation} -s ${device.serial} shell screenrecord --output-format=h264 --time-limit=180 --size=${width}x${height} - "
        //val adbCommand = "ffmpeg -f rawvideo -pix_fmt rgb24 -video_size ${width}x${height} -r 5 -i /dev/urandom -f image2pipe -c:v rawvideo -pix_fmt rgb24 -r 5 -"
        logger.debug(adbCommand)
        return ProcessBuilder()
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .command("sh", "-c", adbCommand)
                .start()
    }

    fun stopAndWait() {
        run = false
        ffmpeg.destroyForcibly()
        imageStream.close()
        interrupt()
        logger.info("Waiting for collector to finish")
        join()
    }

    fun restartCapturing() {
        synchronized(streamLock) {
            ffmpeg.destroyForcibly()
            imageStream.close()

            ffmpeg = startFfmpeg()
            imageStream = ffmpeg.inputStream
        }
    }

    override fun run() {
        logger.info("Starting screenshot collection")
        while (run) {
            val currentImage = IntBuffer.allocate(width * height)
            val dataStream = DataInputStream(imageStream)
            while (run) {

                // Read images
                try {
                    currentImage.put(dataStream.readInt())
                } catch (e: EOFException) {
                    logger.debug("Ffmpeg has died")
                    break
                }
                if (currentImage.remaining() == 0) {
                    // Extract pixels from buffer
                    val pixels = currentImage.array()
                    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    image.raster.setDataElements(0, 0, width, height, pixels)
                    callback(image)
                    currentImage.clear()
                }
            }
            if (run) {
                logger.info("Starting screenshot collection from fresh ffmpeg")
                restartCapturing()
            }
        }
        logger.info("Ending screenshot collection")
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