package io.drojj.lig2.timelapse.maker

import java.time.LocalDateTime
import java.time.temporal.WeekFields.ISO

enum class TimelapseType(
    val targetRootDirectory: String,
    val targetSubDirectoryFunction: (LocalDateTime) -> String
) {
    DAY("days_of_year", { time: LocalDateTime -> time.toLocalDate().toString() }),
    WEEK("weeks_of_year", { time: LocalDateTime -> time.year.toString() + "/" + time[ISO.weekOfWeekBasedYear()].toString() }),
    MONTH("months_of_year", { time: LocalDateTime -> time.year.toString() + "/" + time.month.toString() }),
    YEAR("year", { time: LocalDateTime -> time.year.toString() });
}