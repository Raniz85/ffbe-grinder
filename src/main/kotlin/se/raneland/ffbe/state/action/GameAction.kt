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
        JsonSubTypes.Type(name = "delay", value = DelayAction::class),
        JsonSubTypes.Type(name = "tap", value = TapAction::class),
        JsonSubTypes.Type(name = "selectAbility", value = SelectAbilityAction::class),
        JsonSubTypes.Type(name = "selectItem", value = SelectItemAction::class),
        JsonSubTypes.Type(name = "incrementCounter", value = IncrementCounterAction::class),
        JsonSubTypes.Type(name = "decrementCounter", value = DecrementCounterAction::class),
        JsonSubTypes.Type(name = "setCounter", value = SettCounterAction::class)
)
interface GameAction {

    fun execute(controller: DeviceController, counters: MutableMap<String, Int>)

    fun repeat(): Boolean
}
