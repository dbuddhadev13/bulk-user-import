package com.allride.bulkimport.controller


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.allride.bulkimport.pubsub.EventBus
import com.allride.bulkimport.pubsub.FileUploadedEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class UploadController(private val eventBus: EventBus) {

    private val logger = LoggerFactory.getLogger(UploadController::class.java)

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    suspend fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty || !file.originalFilename.orEmpty().endsWith(".csv")) {
            logger.error("Invalid file. Please upload a non-empty CSV.")
            return ResponseEntity.badRequest().body("Invalid file. Please upload a non-empty CSV.")
        }

        return try {
            val tempFile = withContext(Dispatchers.IO) {
                Files.createTempFile("upload-", ".csv").toFile().apply {
                    file.inputStream.use { input ->
                        outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            // Read the first line (header)
            val headerLine = withContext(Dispatchers.IO) {
                tempFile.useLines { it.firstOrNull()?.trim() ?: "" }
            }

            val expectedHeader = "id,firstName,lastName,email"
            if (headerLine.lowercase() != expectedHeader.lowercase()) {
                tempFile.delete()
                logger.error("Invalid CSV File. Expected a file with following headers: $expectedHeader")
                return ResponseEntity.badRequest().body("Invalid CSV File. Expected a file with following headers: $expectedHeader")
            }

            // Check if the file has at least one data row
            val lineCount = withContext(Dispatchers.IO) {
                tempFile.useLines { it.count() }
            }

            if (lineCount < 2) {
                tempFile.delete()
                logger.error("CSV must have at least one data row.")
                return ResponseEntity.badRequest().body("CSV must have at least one data row.")
            }

            eventBus.publish(FileUploadedEvent(tempFile.absolutePath))
            ResponseEntity.ok("File uploaded successfully. Processing started.")
        } catch (e: Exception) {
            logger.error("Upload failed: ${e.message}", e)
            ResponseEntity.internalServerError().body("Error uploading file: ${e.localizedMessage}")
        }
    }
}
