package io.drojj.lig2.timelapse.maker.video

import io.drojj.lig2.timelapse.maker.TimelapseType
import io.drojj.lig2.timelapse.maker.dao.TimelapseRepository
import io.drojj.lig2.timelapse.maker.utils.Constants
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.imgscalr.Scalr
import org.jcodec.api.awt.AWTSequenceEncoder
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import javax.inject.Inject
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

            val folderToSave =
                "$saveDirectory/${timelapseType.targetRootDirectory}/$targetSubDirectory"
            Files.createDirectories(Paths.get(folderToSave))

            val output = File("$folderToSave/timelapse.mp4")
            val encoder = AWTSequenceEncoder.createSequenceEncoder(output, FPS)
            LOGGER.info("Prepare to encode images")
            for (image in sortedList) {
                var bufferedImage = ImageIO.read(Files.newInputStream(image))
                if (bufferedImage.width != 1280 || bufferedImage.height != 720) {
                    bufferedImage = Scalr.resize(
                        bufferedImage, Scalr.Method.QUALITY,
                        1280, 720
                    )
                }
                encoder.encodeImage(bufferedImage)
                LOGGER.info("Encoded image $image")
            }
            encoder.finish()

            LOGGER.info("Saved raw timelapse to " + output.absolutePath)

            val compressedOutput = "$folderToSave/timelapse265.mp4"
            val job = FFmpegBuilder()
                .setInput(output.absolutePath)
                .overrideOutputFiles(true)
                .addOutput(compressedOutput)
                .setVideoCodec("libx265")
                .setConstantRateFactor(28.0)
                .done()
            ffmpegExecutor.createJob(job, progressListener).run()
            timelapseRepository.saveInformation(File(compressedOutput), timelapseType)
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
        private const val FPS = 5
        private val LOGGER = LoggerFactory.getLogger(TimelapseMakerImpl::class.java)
    }

}