package io.drojj.lig2.timelapse.maker.image

import io.quarkus.cache.CacheResult
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.imageio.ImageIO

@ApplicationScoped
class PreviewCameraHandler(
    @ConfigProperty(name = "lig2.preview.connection-timeout")
    private val connectionTimeout: Int,

    @ConfigProperty(name = "lig2.preview.socket-timeout")
    private val socketTimeout: Int
) : CameraHandler {

    @ConfigProperty(name = "lig2.preview.url")
    lateinit var previewUrl: URL

    @CacheResult(cacheName = "preview-image")
    override fun instantImage(): BufferedImage {
        LOGGER.info("Prepare to download image from {}", previewUrl.toString())
        val connection = previewUrl.openConnection()
        connection.connectTimeout = connectionTimeout
        connection.readTimeout = socketTimeout
        val image = ImageIO.read(connection.getInputStream())
        LOGGER.info("Image read from URL successfully")
        return image
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PreviewCameraHandler::class.java)
    }
}