package io.drojj.configuration

import io.drojj.dao.VideosDAO
import io.drojj.job.ImagePullerJob
import io.drojj.job.JobType
import io.drojj.utils.Constants.VIDEO_DAO
import io.drojj.utils.Constants.IMAGES_DIRECTORY
import io.drojj.utils.Constants.JOB_TYPE
import io.drojj.utils.Constants.PREVIEW_URL
import io.drojj.utils.Constants.VIDEOS_DIRECTORY
import io.quarkus.runtime.StartupEvent
import io.drojj.job.JpegToMp4Job
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.quartz.*
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

@ApplicationScoped
class QuartzConfiguration {

    @Inject
    lateinit var videosDAO: VideosDAO

    @Inject
    lateinit var quartz: Scheduler

    @ConfigProperty(name = "resources.images-directory")
    lateinit var imagesDirectory: String

    @ConfigProperty(name = "resources.videos-directory")
    lateinit var videosDirectory: String

    @ConfigProperty(name = "lig2.preview.url")
    lateinit var previewUrl: URL

    fun onStart(@Observes event: StartupEvent) {
        JobType.values().forEach { scheduleJobs(it) }
    }

    private fun scheduleJobs(jobType: JobType) {
        val jobData = JobDataMap()
        jobData[IMAGES_DIRECTORY] = imagesDirectory
        jobData[VIDEOS_DIRECTORY] = videosDirectory
        jobData[PREVIEW_URL] = previewUrl
        jobData[JOB_TYPE] = jobType
        jobData[VIDEO_DAO] = videosDAO

        val jobTypeLower = jobType.name.lowercase()
        val imageJob = JobBuilder.newJob(ImagePullerJob::class.java)
            .withIdentity(jobTypeLower, IMAGE_PULLERS_GROUP)
            .usingJobData(jobData)
            .build()

        val imageTrigger = TriggerBuilder.newTrigger()
            .withIdentity("$jobTypeLower-trigger", IMAGE_PULLERS_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(jobType.imagePullCron))
            .forJob(imageJob.key.name, imageJob.key.group)
            .build()

        val videoJob = JobBuilder.newJob(JpegToMp4Job::class.java)
            .withIdentity(jobTypeLower, JPEG_TO_MP4_GROUP)
            .usingJobData(jobData)
            .build()

        val videoTrigger = TriggerBuilder.newTrigger()
            .withIdentity("$jobTypeLower-trigger", JPEG_TO_MP4_GROUP)
            .withSchedule(CronScheduleBuilder.cronSchedule(jobType.jpegToMp4Cron))
            .forJob(videoJob.key.name, videoJob.key.group)
            .build()

        quartz.scheduleJob(imageJob, imageTrigger)
        quartz.scheduleJob(videoJob, videoTrigger)
    }

    companion object {
        private const val IMAGE_PULLERS_GROUP = "image-pullers"
        private const val JPEG_TO_MP4_GROUP = "jpeg-to-mp4"
    }

}