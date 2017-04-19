/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.ui

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import javax.swing.JTextArea
import javax.swing.SwingUtilities

/**
 * @author Raniz
 * @since 2017-04-14.
 */
class TextViewAppender(val textArea: JTextArea) : AppenderBase<ILoggingEvent>() {
    val lines = mutableListOf<String>()

    override fun append(event: ILoggingEvent) {
        val text = synchronized(lines) {
            if (event.level.levelInt < Level.INFO_INT) {
                return
            }
            when (event.level.levelInt) {
                Level.INFO_INT -> lines.add(event.message)
                else -> lines.add("${event.level.levelStr}: ${event.message}")
            }
            lines.joinToString("\n")
        }
        SwingUtilities.invokeLater {
            textArea.text = text
        }
    }

}
