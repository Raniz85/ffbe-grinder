/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import se.raneland.ffbe.config.ImageRegionConfig
import spock.lang.Specification

/**
 * @author Raniz
 * @since 2017-01-21.
 */
class ImageRegionTest extends Specification {

    BufferedImage read(path) {
        return getClass().getResourceAsStream(path).withStream {
            ImageIO.read(it)
        }
    }

    def "That images can be matched"() {
        given: "An image region and a screenshot"
        def screenshot = read("/earth-shrine-screenshot.png")
        def region = ImageRegionConfig.earthShrine()

        expect: "The region to match the screenshot"
        region.matches(screenshot)
    }
}
