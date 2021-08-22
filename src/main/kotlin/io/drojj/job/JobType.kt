package io.drojj.job

import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.function.Function

enum class JobType(
    val imagePullCron: String,
    val jpegToMp4Cron: String,
    val targetRootDirectory: String,
    val targetSubDirectoryFunction: Function<LocalDateTime, String>
) {

    DAY("0 */2 8-20 ? * *",
        "0 30 21 ? * * *",
        "days_of_year", { localDateTime: LocalDateTime -> localDateTime.toLocalDate().toString() }),
    WEEK("0 */15 8-20 ? * *",
        "0 30 21 ? * SUN *",
        "weeks_of_year",
        { localDateTime: LocalDateTime -> localDateTime.year.toString() + "/" + localDateTime[WeekFields.ISO.weekOfWeekBasedYear()].toString() }),
    MONTH("0 0 8-20 ? * *",
        "0 30 21 L * ? *",
        "months_of_year",
        { localDateTime: LocalDateTime -> localDateTime.year.toString() + "/" + localDateTime.month.toString() }),
    YEAR("0 0 12 * * ?",
        "0 30 12 L DEC ? *",
        "year", { localDateTime: LocalDateTime -> localDateTime.year.toString() });
}