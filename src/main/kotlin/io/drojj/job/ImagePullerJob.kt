package io.drojj.job

import io.drojj.utils.Constants.IMAGE_BASENAME_FORMATTER
import io.drojj.utils.Constants.IMAGES_DIRECTORY
import io.drojj.utils.Constants.JOB_TYPE
import io.drojj.utils.Constants.PREVIEW_URL
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.imageio.ImageIO

class ImagePullerJob : Job {

    override fun execute(context: JobExecutionContext) {
        val now = LocalDateTime.now()
        val imagesDirectory = context.jobDetail.jobDataMap.getString(IMAGES_DIRECTORY)
        val previewUrl = context.jobDetail.jobDataMap[PREVIEW_URL] as URL
        val jobType = context.jobDetail.jobDataMap[JOB_TYPE] as JobType
        LOGGER.info("Job {} starting", jobType.name)

        try {
            LOGGER.info("Prepare to download image from {}", previewUrl.toString())
            val connection = previewUrl.openConnection()
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            val bufferedImage = ImageIO.read(connection.getInputStream())
            LOGGER.info("Image read from URL successfully")

            val folderToSave =
                "$imagesDirectory/${jobType.targetRootDirectory}/${jobType.targetSubDirectoryFunction.apply(now)}"
            Files.createDirectories(Paths.get(folderToSave))

            val filePath = "$folderToSave/${IMAGE_BASENAME_FORMATTER.format(now)}.jpg"
            ImageIO.write(bufferedImage, "jpg", File(filePath))
            LOGGER.info("Successfully saved image at $filePath")
        } catch (e: Exception) {
            LOGGER.error("Can't download and save", e)
            LOGGER.info("Skip image pull")
        }
    }

    companion object {
        private const val CONNECTION_TIMEOUT = 1000
        private const val READ_TIMEOUT = 30 * 1000
        private val LOGGER = LoggerFactory.getLogger(ImagePullerJob::class.java)
    }
}