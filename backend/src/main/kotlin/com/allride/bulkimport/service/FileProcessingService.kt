package com.allride.bulkimport.service

import com.allride.bulkimport.model.User
import com.allride.bulkimport.pubsub.EventBus
import com.allride.bulkimport.pubsub.FileUploadedEvent
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileProcessingService(eventBus: EventBus) {

    private val logger = LoggerFactory.getLogger(FileProcessingService::class.java)
    private val inMemoryStorage = mutableListOf<User>()

    init {
        logger.info("🛠️ FileProcessingService initialized")
        eventBus.subscribe { event ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    processFile(event.path)
                } catch (e: Exception) {
                    logger.error("Error during file processing: ${e.message}", e)
                } finally {
                    cleanupFile(event.path)
                }
            }
        }
    }

    private suspend fun processFile(path: String) {
        val file = File(path)
        if (!file.exists()) {
            logger.warn("File not found: $path")
            return
        }

        logger.info("Processing file: $path")
        file.useLines { lines ->
            lines.drop(1).forEachIndexed { index, line ->
                try {
                    val tokens = line.split(",").map { it.trim() }
                    if (tokens.size < 4) throw IllegalArgumentException("Line has insufficient columns")

                    val user = User(
                        id = tokens[0],
                        firstName = tokens[1],
                        lastName = tokens[2],
                        email = tokens[3]
                    )
                    validateRow(user)
                    inMemoryStorage.add(user)
                    logger.info("Imported (line ${index + 2}): $user")
                } catch (ex: Exception) {
                    logger.warn("Validation failed at line ${index + 2}: ${ex.message}")
                }
            }
        }

        logger.info("Total valid users stored: ${inMemoryStorage.size - 2}")
    }

    private fun validateRow(user: User) {
        if (user.id.isBlank()) throw IllegalArgumentException("ID is required")
        if (user.firstName.isBlank()) throw IllegalArgumentException("First name is required")
        if (user.lastName.isBlank()) throw IllegalArgumentException("Last name is required")
        if (!user.email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            throw IllegalArgumentException("Invalid email: ${user.email}")
        }
    }

    private fun cleanupFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                logger.info("Temp file deleted: $path")
            }
        } catch (e: Exception) {
            logger.warn("Could not delete temp file: $path")
        }
    }
}
