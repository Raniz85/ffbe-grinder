/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state

import mu.KLogging
import se.raneland.ffbe.service.DeviceController
import se.raneland.ffbe.state.action.GameAction
import se.raneland.ffbe.state.transition.StateTransition
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.ZonedDateTime
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque

/**
 * @author Raniz
 * @since 2017-01-14.
 */
class StateMachine(private val device: DeviceController, private val initialState: MachineState) {

    companion object : KLogging()

    private val listeners = CopyOnWriteArrayList<(GameState) -> Unit>()

    private var lastTransition = ZonedDateTime.now()
    private var state = initialState

    private var evaluator: TransitionEvaluator
    private var executor: ActionExecutor

    val counters: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    init {
        evaluator = TransitionEvaluator(initialState.transitions)
        evaluator.start()
        executor = ActionExecutor(initialState.actions)
        executor.start()
    }

    fun reset() {
        setState(initialState)
    }

    fun addListener(listener: (GameState) -> Unit) = listeners.add(listener)

    fun removeListener(listener: (GameState) -> Unit) = listeners.remove(listener)

    private fun setState(nextState: MachineState) {
        // Stop the current state
        evaluator.stopEvaluating()
        executor.stopExecuting()

        // Update the state
        logger.info("Entering state ${nextState.name}")
        state = nextState
        lastTransition = ZonedDateTime.now()
        if (state.transitions.isEmpty()) {
            logger.warn("Entered state with no transitions")
        }

        // Start the threads again
        evaluator = TransitionEvaluator(state.transitions)
        evaluator.start()
        executor = ActionExecutor(state.actions)
        executor.start()
    }

    fun enqueue(screen: BufferedImage) {
        evaluator.push(screen)
    }

    fun stop() {
        evaluator.stopEvaluating()
        executor.stopExecuting()
    }

    private inner class TransitionEvaluator(val transitions: List<StateTransition>): Thread("transition-evaluator") {

        private @Volatile var run = true

        private val queue = LinkedBlockingDeque<BufferedImage>()

        fun push(screen: BufferedImage) {
            queue.addLast(screen)
            if (queue.size > 10) {
                logger.warn("Evaluation queue contains ${queue.size} items in state ${state} is, reduce the amount of transitions or get a faster computer")
            }
        }

        fun stopEvaluating() {
            run = false
            interrupt()
        }

        override fun run() {
            try {
                while (run) {
                    val screen = queue.take()
                    val now = ZonedDateTime.now()
                    val gameState = GameState(screen, Duration.between(lastTransition, now), counters, executor.actionQueue.toList())
                    listeners.forEach { it.invoke(gameState) }
                    logger.trace("Game state : ${gameState}")
                    val nextState = transitions.firstOrNull {
                        logger.trace("Testing transition ${it}")
                        it.test.matches(gameState)
                    }?.nextState ?: continue
                    queue.clear()
                    setState(nextState)
                    break
                }
            } catch (e: InterruptedException) {
                // Do nothing
            }
        }
    }

    private inner class ActionExecutor(actions: List<GameAction>): Thread("action-executor") {

        private @Volatile var run = true

        val actionQueue = LinkedBlockingDeque(actions)

        fun stopExecuting() {
            run = false
            interrupt()
        }

        override fun run() {
            try {
                while (actionQueue.isNotEmpty() && run) {
                    val action = actionQueue.peek()
                    logger.info("Executing action ${action}")
                    action.execute(device, counters)
                    if (action.repeat()) {
                        actionQueue.addLast(action)
                    }
                    actionQueue.removeFirst()
                }
            } catch (e: InterruptedException) {
                // Do nothing
            } catch(e: NoSuchElementException) {

            }
        }
    }

}

