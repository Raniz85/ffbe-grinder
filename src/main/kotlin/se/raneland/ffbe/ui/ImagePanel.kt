/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.ui

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * @author Raniz
 * @since 2017-01-14.
 */
class ImagePanel : JPanel {

    var image: BufferedImage? = null
    set(value) {
        field =  value
        resizeImage()
        SwingUtilities.invokeLater {
            repaint()
        }
    }

    private var resizedImage: Image? = null

    constructor() : super() {
        addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                resizeImage()
            }
        })
    }

    override fun paintComponent(g0: Graphics?) {
        val g = g0 as Graphics2D
        val image = this.resizedImage ?: return
        g.color = Color.BLACK
        g.fillRect(0, 0, size.width, size.height)
        g.drawImage(image, 0, 0, null)
    }

    private fun resizeImage() {
        // Maintain aspect ration
        val size = this.size
        val widthByHeight = (size.height / 16.0 * 9).toInt()
        val heightByWidth = (size.width / 9.0 * 16).toInt()
        val width = Math.min(size.width, widthByHeight)
        val height = Math.min(size.height, heightByWidth)
        this.resizedImage = image?.getScaledInstance(width, height, Image.SCALE_DEFAULT)
    }
}
