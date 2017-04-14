/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.transition

import se.raneland.ffbe.image.ImageRegion
import se.raneland.ffbe.state.GameState
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * @author Raniz
 * @since 2017-04-14.
 */
class ImageRegionTransitionTest : TransitionTest {

    val region: ImageRegion

    constructor(name: String, x: Int, y: Int) {
        val path = Paths.get(name).takeIf { Files.exists(it) }
        val image = if (path != null) {
            ImageIO.read(path.toUri().toURL())
        } else {
            val stream = ImageRegionTransitionTest::class.java.getResourceAsStream("/images/${name}") ?: error("Image ${name} could not be found")
            stream.use {
               ImageIO.read(it)
            }
        }
        region = ImageRegion(image, x, y)
    }

    override fun matches(gameState: GameState): Boolean {
        return region.matches(gameState.screen)
    }

}
