package io.drojj.lig2.timelapse.maker.jobs

import io.drojj.lig2.timelapse.maker.TimelapseType
import io.drojj.lig2.timelapse.maker.image.ImageSaver
import io.quarkus.scheduler.Scheduled
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class ScheduledImagesSaver {

    @Inject
    lateinit var imageSaver: ImageSaver

    @Scheduled(cron = "0 */2 8-20 ? * *")
    fun dailyImages() {
        saveImage(TimelapseType.DAY)
    }

    @Scheduled(cron = "0 */15 8-20 ? * *")
    fun weekImages() {
        saveImage(TimelapseType.WEEK)
    }

    @Scheduled(cron = "0 0 8-20 ? * *")
    fun monthImages() {
        saveImage(TimelapseType.MONTH)
    }

    @Scheduled(cron = "0 0 12 * * ?")
    fun yearImages() {
        saveImage(TimelapseType.YEAR)
    }

    private fun saveImage(type: TimelapseType) {
        try {
            imageSaver.saveImage(type)
        } catch (e: Exception) {
            LOGGER.error("Got exception while saving image", e)
            LOGGER.info("Skip image pull")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ScheduledImagesSaver::class.java)
    }

}