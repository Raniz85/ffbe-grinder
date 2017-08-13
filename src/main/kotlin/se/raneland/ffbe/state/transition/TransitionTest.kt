/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.transition

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import se.raneland.ffbe.state.GameState

/**
 * @author Raniz
 * @since 2017-04-10.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(name = "time", value = TimedTransitionTest::class),
        JsonSubTypes.Type(name = "imageRegion", value = ImageRegionTransitionTest::class),
        JsonSubTypes.Type(name = "allActionsExecuted", value = AllActionsExecutedTest::class),
        JsonSubTypes.Type(name = "counterValue", value = CounterValueTest::class)
)
interface TransitionTest {

    fun matches(gameState: GameState): Boolean
}
