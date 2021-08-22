package io.drojj.job

import io.drojj.dao.VideosDAO
import io.drojj.utils.Constants.IMAGES_DIRECTORY
import io.drojj.utils.Constants.IMAGE_BASENAME_FORMATTER
import io.drojj.utils.Constants.JOB_TYPE
import io.drojj.utils.Constants.VIDEO_DAO
import org.jcodec.api.awt.AWTSequenceEncoder
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.imageio.ImageIO
import kotlin.streams.toList

class JpegToMp4Job : Job {

    override fun execute(context: JobExecutionContext) {
        val now = LocalDateTime.now()
        val videosDAO = context.jobDetail.jobDataMap[VIDEO_DAO] as VideosDAO
        val jobType = context.jobDetail.jobDataMap[JOB_TYPE] as JobType
        val imagesDirectory =
            "${context.jobDetail.jobDataMap.getString(IMAGES_DIRECTORY)}/${jobType.targetRootDirectory}/${
                jobType.targetSubDirectoryFunction.apply(now)
            }"
        val saveDirectory = context.jobDetail.jobDataMap.getString(VIDEO_DAO)

        var fileSaved = false
        val jpegPath = Paths.get(imagesDirectory)
        try {
            val sortedList = Files.list(jpegPath).sorted { o1, o2 ->
                val fileName1 = o1.fileName.toString()
                val fileName2 = o2.fileName.toString()
                LocalDateTime.parse(fileName1.substring(0, fileName1.lastIndexOf('.')), IMAGE_BASENAME_FORMATTER)
                    .compareTo(
                        LocalDateTime.parse(
                            fileName2.substring(0, fileName2.lastIndexOf('.')),
                            IMAGE_BASENAME_FORMATTER
                        )
                    )
            }.toList()

            if (sortedList.isEmpty()) {
                LOGGER.info("No files found at $jpegPath. Exiting")
                return
            }

            val folderToSave =
                "$saveDirectory/${jobType.targetRootDirectory}/${jobType.targetSubDirectoryFunction.apply(now)}"
            Files.createDirectories(Paths.get(folderToSave))

            val output = File("$folderToSave/timelapse.mp4")
            val encoder = AWTSequenceEncoder.createSequenceEncoder(output, FPS)
            LOGGER.info("Prepare to encode images")
            for (image in sortedList) {
                encoder.encodeImage(ImageIO.read(Files.newInputStream(image)))
                LOGGER.info("Encoded image $image")
            }
            encoder.finish()

            LOGGER.info("Saved video to " + output.absolutePath)
            videosDAO.saveVideoInformation(output, jobType)
            fileSaved = true
        } catch (e: Exception) {
            LOGGER.error("Can't create video, rescheduling", e)
            throw JobExecutionException(e, true)
        } finally {
            if (fileSaved) {
                LOGGER.info("Prepare to remove jpeg files")
                Files.list(jpegPath).map { obj: Path -> obj.toFile() }.forEach { obj: File -> obj.delete() }
                LOGGER.info("All jpeg were deleted")
            }
        }
    }

    companion object {
        private const val FPS = 5
        private val LOGGER = LoggerFactory.getLogger(JpegToMp4Job::class.java)
    }
}