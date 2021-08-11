package io.drojj.utils

import java.time.format.DateTimeFormatter

object Constants {
    const val PREVIEW_URL = "PREVIEW_URL"
    const val JOB_TYPE = "JOB_TYPE"
    const val IMAGES_DIRECTORY = "IMAGES_DIRECTORY"
    const val VIDEOS_DIRECTORY = "VIDEOS_DIRECTORY"
    val IMAGE_BASENAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH_mm_ss")
}