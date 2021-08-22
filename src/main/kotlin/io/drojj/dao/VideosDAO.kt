package io.drojj.dao

import io.drojj.job.JobType
import java.io.File

interface VideosDAO {

    fun saveVideoInformation(file: File, jobType: JobType)

}