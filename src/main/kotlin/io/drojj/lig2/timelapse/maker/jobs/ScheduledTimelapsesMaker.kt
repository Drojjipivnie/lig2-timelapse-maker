package io.drojj.lig2.timelapse.maker.jobs

import io.drojj.lig2.timelapse.maker.TimelapseType
import io.drojj.lig2.timelapse.maker.video.TimelapseMaker
import io.quarkus.scheduler.Scheduled
import org.quartz.JobExecutionException
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class ScheduledTimelapsesMaker {

    @Inject
    lateinit var timelapseMaker: TimelapseMaker

    @Scheduled(cron = "0 15 21 ? * * *")
    fun dailyTimelapses() {
        makeTimelapse(TimelapseType.DAY)
    }

    @Scheduled(cron = "0 15 21 ? * SUN *")
    fun weekTimelapses() {
        makeTimelapse(TimelapseType.WEEK)
    }

    @Scheduled(cron = "0 15 21 L * ? *")
    fun monthTimelapses() {
        makeTimelapse(TimelapseType.MONTH)
    }

    @Scheduled(cron = "0 15 12 L DEC ? *")
    fun yearTimelapses() {
        makeTimelapse(TimelapseType.YEAR)
    }

    private fun makeTimelapse(type: TimelapseType) {
        try {
            timelapseMaker.makeTimelapse(type)
        } catch (e: Exception) {
            LOGGER.error("Can't create timelapse, rescheduling", e)
            throw JobExecutionException(e, true)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ScheduledTimelapsesMaker::class.java)
    }

}