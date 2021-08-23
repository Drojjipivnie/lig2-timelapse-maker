package io.drojj.lig2.timelapse.maker.utils

import java.time.format.DateTimeFormatter

object Constants {
    val IMAGE_BASENAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH_mm_ss")
}