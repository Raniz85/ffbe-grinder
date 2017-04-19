/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.ui

import ch.qos.logback.classic.Logger
import net.miginfocom.swing.MigLayout
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import se.raneland.ffbe.service.DeviceController
import se.raneland.ffbe.service.Point
import se.raneland.ffbe.state.StateMachine
import se.vidstige.jadb.JadbDevice
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@Component
class MainWindow// Set up the UI
@Autowired constructor(val deviceController: DeviceController, context: ConfigurableApplicationContext) : JFrame("Final Fantasy Brave Exvius Controller") {

    var machine: StateMachine? = null

    // UI components
    val screenshot: ImagePanel
    val deviceList: JComboBox<Device>
    val logScroll: JScrollPane
    val logView: JTextArea
    val startButton: JButton
    val stopButton: JButton

    var logAppender: TextViewAppender? = null

    init {
        preferredSize = Dimension(900, 850)
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                context.close()
                System.exit(0)
            }
        })
        layout = MigLayout("fill", "[grow 0, shrink 0][fill][fill]", "[][][fill]")
        screenshot = ImagePanel()
        screenshot.preferredSize = Dimension(450, 800)
        screenshot.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val x = e.x / screenshot.width.toDouble()
                val y = e.y / screenshot.height.toDouble()
                deviceController.tap(Point(x, y))
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
        add(deviceList, "cell 1 0 2 1")

        startButton = JButton("Start")
        stopButton = JButton("stop")
        stopButton.isEnabled = false

        startButton.addActionListener {
            var runningMachine = machine
            if (runningMachine != null) {
                runningMachine.stop()
            }
            val stateGraph = GraphSelector(this).select()
            this.machine = stateGraph?.let {
                StateMachine(deviceController, it.initialState)
            }
            startButton.isEnabled = this.machine ==null
            stopButton.isEnabled = this.machine != null
        }
        add(startButton, "cell 1 1")

        stopButton.addActionListener {
            var runningMachine = machine
            if (runningMachine != null) {
                runningMachine.stop()
            }
            this.machine = null
            startButton.isEnabled = true
            stopButton.isEnabled = false
        }
        add(stopButton, "cell 2 1")


        logView = JTextArea()
        logView.lineWrap = true
        logScroll = JScrollPane(logView)
        logScroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        add(logScroll, "cell 1 2 2 1")

        pack()
        deviceController.addScreenshotListener {
            screenshot.image = it
            machine?.enqueue(it)
        }
        deviceController.collectScreenshots = true
    }

    private fun startLogging() {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

        val runningAppender = logAppender
        if (runningAppender != null) {
            runningAppender.stop()
            rootLogger.detachAppender(runningAppender)
        }
        val appender = TextViewAppender(logView)
        rootLogger.addAppender(appender)
        appender.start()
        logAppender = appender
    }

    class Device(val device: JadbDevice) {
        override fun toString() = device.serial
    }

}
