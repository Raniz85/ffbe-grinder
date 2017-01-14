/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import se.raneland.ffbe.ui.MainWindow

/**
 * @author Raniz
 * @since 2017-01-14.
 */
@SpringBootApplication
class FfBeApp {
}

fun main(args: Array<String>) {
    val context = SpringApplication.run(FfBeApp::class.java, *args)
    context.getBean(MainWindow::class.java).isVisible = true
}