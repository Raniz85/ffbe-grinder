/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.ui

import net.miginfocom.swing.MigLayout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import se.raneland.ffbe.service.DeviceController
import se.raneland.ffbe.state.StateMachine
import se.raneland.ffbe.state.listAvailableGraphs
import se.raneland.ffbe.state.readStateGraph
import se.vidstige.jadb.JadbDevice
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.WindowConstants

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@Component
class MainWindow// Set up the UI
@Autowired constructor(val deviceController: DeviceController, context: ConfigurableApplicationContext) : JFrame("Final Fantasy Brave Exvius Controller") {

    var machine: StateMachine? = null
    val availableGraphs = listAvailableGraphs()

    // UI components
    val screenshot: ImagePanel
    val deviceList: JComboBox<Device>
    val graphList: JComboBox<String>
    val logScroll: JScrollPane
    val logView: JTextArea

    init {
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                context.close()
                System.exit(0)
            }
        })
        layout = MigLayout("fill", "[grow 0][fill]", "[][][fill]")
        screenshot = ImagePanel()
        screenshot.preferredSize = Dimension(450, 800)
        screenshot.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val x = e.x / screenshot.width.toDouble()
                val y = e.y / screenshot.height.toDouble()
                deviceController.tap(x, y)
            }
        })
        add(screenshot, "cell 0 0 1 3")

        deviceList = JComboBox()
        deviceController.devices.forEach {
            deviceList.addItem(Device(it))
        }
        deviceList.addActionListener {
            val item = deviceList.selectedItem
            if (item is Device) {
                deviceController.currentDevice = item.device
            }
        }
        add(deviceList, "cell 1 0")

        graphList = JComboBox()
        availableGraphs.keys.forEach(graphList::addItem)
        graphList.addActionListener {
            val name = graphList.selectedItem
            val url = availableGraphs[name]!!
            val contents = url.openStream().use {
                val size = it.available()
                it.readBytes(size)
            }.toString(StandardCharsets.UTF_8)
            val initialState = readStateGraph(contents)
            val machine = StateMachine(deviceController, initialState)
            this.machine = machine
        }
        add(graphList, "cell 1 1")

        logView = JTextArea()
        logScroll = JScrollPane(logView)
        add(logScroll, "cell 1 2")


        pack()
        deviceController.addScreenshotListener {
            screenshot.image = it
            machine?.enqueue(it)
        }
        deviceController.collectScreenshots = true
    }

    class Device(val device: JadbDevice) {
        override fun toString() = device.serial
    }

}
