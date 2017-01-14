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
import se.vidstige.jadb.JadbDevice
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.util.function.Consumer
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.WindowConstants

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@Component
class MainWindow : JFrame {

    val deviceController: DeviceController

    // UI components
    val screenshot: ImagePanel
    val deviceList: JComboBox<Device>

    @Autowired
    constructor(deviceController: DeviceController, context: ConfigurableApplicationContext) : super("Final Fantasy Brave Exvius Controller") {
        this.deviceController = deviceController

        // Set up the UI
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                context.close()
                System.exit(0)
            }
        })

        layout = MigLayout()

        screenshot = ImagePanel()
        screenshot.preferredSize = Dimension(450, 800)
        add(screenshot, "span 1 2")

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
        add(deviceList)

        pack()

        deviceController.addScreenshotListener {
            screenshot.image = it
        }
        deviceController.collectScreenshots = true
    }

    class Device(val device: JadbDevice) {
        override fun toString() = device.serial
    }

}
