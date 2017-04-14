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
        if (event.level.levelInt < Level.INFO_INT) {
            return
        }
        lines.add(event.message)
        SwingUtilities.invokeLater {
            textArea.text = lines.joinToString("\n")
        }
    }

}
