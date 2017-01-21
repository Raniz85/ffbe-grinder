/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Raniz
 * @since 2017-01-21.
 */
@Service
class ImageMatcher(val controller: DeviceController, val regions: List<ImageRegion>) : Thread("imagematcher") {

    @Volatile
    private var run: Boolean = true;

    private val exchanger: Exchanger<BufferedImage> = Exchanger()

    private val executor = Executors.newCachedThreadPool()

    init {
        isDaemon = true
    }

    override fun run() {
        while (run) {
            val image = exchanger.get()
            regions.forEach { region ->
                executor.execute { region.match(image) }
            }
        }
    }

    @PostConstruct
    fun startImageMatching() {
        run = true
        start()
        controller.addScreenshotListener {
            exchanger.put(it)
        }
    }

    @PreDestroy
    fun stopImageMatching() {
        run = false
    }

}

private class Exchanger<T> {
    @Volatile
    var item: T? = null

    val mutex = Object()

    fun get(): T {
        synchronized(mutex) {
            var item = this.item
            while (item == null) {
                mutex.wait()
                item = this.item
            }
            this.item = null
            return item
        }
    }

    fun put(item: T)  {
        synchronized(mutex) {
            this.item = item
            mutex.notify()
        }
    }
}
