/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.service

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * @author Raniz
 * @since 2017-01-14.
 */
data class Point(val x: Double, val y: Double) {

    @JsonCreator
    constructor(string: String) : this(string.split(",")[0].toDouble(), string.split(",")[1].toDouble())
}
