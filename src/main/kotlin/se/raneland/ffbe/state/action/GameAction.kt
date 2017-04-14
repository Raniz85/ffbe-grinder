/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.action

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import se.raneland.ffbe.service.DeviceController

/**
 * @author Raniz
 * @since 2017-04-10.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(name = "tap", value = TapAction::class)
)
interface GameAction {

    fun execute(controller: DeviceController)

    fun repeat(): Boolean
}
