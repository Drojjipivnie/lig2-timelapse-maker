package io.drojj.lig2.timelapse.maker.image

import io.drojj.lig2.timelapse.maker.TimelapseType
import io.drojj.lig2.timelapse.maker.utils.Constants.IMAGE_BASENAME_FORMATTER
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO
import javax.inject.Inject

@ApplicationScoped
class FilesystemImageSaver : ImageSaver {

    @ConfigProperty(name = "resources.images-directory")
    lateinit var imagesDirectory: String

    @Inject
    lateinit var cameraHandler: CameraHandler

    override fun saveImage(timelapseType: TimelapseType) {
        LOGGER.info("Starting process of saving image for {}", timelapseType.name)
        val now = LocalDateTime.now()
        val image = cameraHandler.instantImage()

        val folderToSave = "$imagesDirectory/${timelapseType.targetRootDirectory}/${timelapseType.targetSubDirectoryFunction(now)}"
        Files.createDirectories(Paths.get(folderToSave))

        val filePath = "$folderToSave/${IMAGE_BASENAME_FORMATTER.format(now)}.jpg"
        ImageIO.write(image, "jpg", File(filePath))
        LOGGER.info("Successfully saved image at $filePath")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(FilesystemImageSaver::class.java)
    }

}