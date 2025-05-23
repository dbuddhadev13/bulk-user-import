package com.allride.bulkimport.pubsub

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventBus {

    private val logger = LoggerFactory.getLogger(EventBus::class.java)
    private val channel = Channel<FileUploadedEvent>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var subscribers = mutableListOf<suspend (FileUploadedEvent) -> Unit>()

    init {
        logger.info("EventBus initialized and listening for events")
        scope.launch {
            for (event in channel) {
                logger.info("Event received: ${event.path}")
                subscribers.forEach { subscriber ->
                    try {
                        subscriber(event)
                    } catch (ex: Exception) {
                        logger.error("Error in subscriber: ${ex.message}", ex)
                    }
                }
            }
        }
    }

    fun subscribe(handler: suspend (FileUploadedEvent) -> Unit) {
        logger.info("Subscriber registered")
        subscribers.add(handler)
    }

    suspend fun publish(event: FileUploadedEvent) {
        logger.info("Publishing event for file: ${event.path}")
        channel.send(event)
    }
}
