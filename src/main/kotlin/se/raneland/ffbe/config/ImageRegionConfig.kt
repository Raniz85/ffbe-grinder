/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.config

import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import se.raneland.ffbe.service.ImageRegion
import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.util.ArrayList
import javax.imageio.ImageIO

/**
 * @author Raniz
 * @since 2017-01-21.
 */
@Configuration
class ImageRegionConfig {

    companion object : KLogging() {
        @JvmStatic
        @JvmOverloads
        fun earthShrine(action: () -> Unit = {}) = ImageRegion(read("/regions/earth-shrine.png"), 143, 261, action)

        private fun read(path: String): BufferedImage {
            val stream = javaClass.getResourceAsStream(path) ?: throw FileNotFoundException(path)
            return stream.use { ImageIO.read(it) }
        }
    }

    @Bean
    fun regions(): ArrayList<ImageRegion> {
        val regions = ArrayList<ImageRegion>();

        regions.add(earthShrine { logger.info("At Earth Shrine quest selection") })

        return regions
    }
}
