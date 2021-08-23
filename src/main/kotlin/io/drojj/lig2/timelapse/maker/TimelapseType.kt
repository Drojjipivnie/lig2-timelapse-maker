package io.drojj.lig2.timelapse.maker

import org.threeten.extra.YearWeek
import java.time.LocalDateTime
import java.time.YearMonth

enum class TimelapseType(
    val targetRootDirectory: String,
    val targetSubDirectoryFunction: (LocalDateTime) -> String
) {
    DAY("days_of_year", { time: LocalDateTime -> time.toLocalDate().toString() }),
    WEEK("weeks_of_year", { time: LocalDateTime -> YearWeek.from(time).toString() }),
    MONTH("months_of_year", { time: LocalDateTime -> YearMonth.from(time).toString() }),
    YEAR("year", { time: LocalDateTime -> time.year.toString() });
}