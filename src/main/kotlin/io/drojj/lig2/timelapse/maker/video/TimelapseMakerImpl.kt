package io.drojj.lig2.timelapse.maker.video

import io.drojj.lig2.timelapse.maker.TimelapseType
import io.drojj.lig2.timelapse.maker.dao.TimelapseRepository
import io.drojj.lig2.timelapse.maker.utils.Constants
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.apache.commons.lang3.math.Fraction
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.io.path.absolutePathString
import kotlin.streams.toList

@ApplicationScoped
class TimelapseMakerImpl : TimelapseMaker {

    @ConfigProperty(name = "resources.images-directory")
    lateinit var imagesDirectory: String

    @ConfigProperty(name = "resources.timelapses-directory")
    lateinit var saveDirectory: String

    @Inject
    lateinit var timelapseRepository: TimelapseRepository

    val ffmpegExecutor = FFmpegExecutor(FFmpeg(), FFprobe())

    val progressListener = HumanReadableProgressListener()

    override fun makeTimelapse(timelapseType: TimelapseType) {
        val now = LocalDateTime.now()
        val targetSubDirectory = timelapseType.targetSubDirectoryFunction(now)
        val imagesDirectory = "$imagesDirectory/${timelapseType.targetRootDirectory}/$targetSubDirectory"

        var fileSaved = false
        val jpegPath = Paths.get(imagesDirectory)
        try {
            val sortedList = Files.list(jpegPath).sorted { o1, o2 ->
                val fileName1 = o1.fileName.toString()
                val fileName2 = o2.fileName.toString()
                LocalDateTime.parse(
                    fileName1.substring(0, fileName1.lastIndexOf('.')),
                    Constants.IMAGE_BASENAME_FORMATTER
                )
                    .compareTo(
                        LocalDateTime.parse(
                            fileName2.substring(0, fileName2.lastIndexOf('.')),
                            Constants.IMAGE_BASENAME_FORMATTER
                        )
                    )
            }.toList()

            if (sortedList.isEmpty()) {
                LOGGER.info("No files found at $jpegPath. Exiting")
                return
            }

            val frameOrderFile = Files.createTempFile(timelapseType.name, ".txt")
            val frameOrderFilePath = frameOrderFile.toAbsolutePath().toString()
            BufferedWriter(OutputStreamWriter(FileOutputStream(frameOrderFile.toFile()))).use {
                for (image in sortedList) {
                    it.write("file '${image.absolutePathString()}'")
                    it.newLine()
                    it.write("duration 0.2")
                    it.newLine()
                }
            }

            val folderToSave =
                "$saveDirectory/${timelapseType.targetRootDirectory}/$targetSubDirectory"
            Files.createDirectories(Paths.get(folderToSave))

            LOGGER.info("Prepared frame order in file {}", frameOrderFilePath)

            val targetVideoPath = "$folderToSave/timelapse.mp4"
            val job = FFmpegBuilder()
                .addExtraArgs("-safe", "0")
                .setInput(frameOrderFilePath)
                .setFormat("concat")
                .overrideOutputFiles(true)
                .addOutput(targetVideoPath)
                .setVideoCodec("libx265")
                .setVideoFrameRate(FPS)
                .setVideoResolution(1280, 720)
                .setConstantRateFactor(28.0)
                .done()
            ffmpegExecutor.createJob(job, progressListener).run()
            timelapseRepository.saveInformation(File(targetVideoPath), timelapseType)
            fileSaved = true
        } finally {
            if (fileSaved) {
                LOGGER.info("Prepare to remove jpeg files")
                Files.list(jpegPath).map { obj: Path -> obj.toFile() }.forEach { obj: File -> obj.delete() }
                LOGGER.info("All jpeg were deleted")
            }
        }
    }

    companion object {
        private val FPS = Fraction.getFraction(5.0)
        private val LOGGER = LoggerFactory.getLogger(TimelapseMakerImpl::class.java)
    }

}