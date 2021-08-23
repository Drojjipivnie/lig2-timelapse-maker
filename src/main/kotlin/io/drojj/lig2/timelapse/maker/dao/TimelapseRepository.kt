package io.drojj.lig2.timelapse.maker.dao

import io.drojj.lig2.timelapse.maker.TimelapseType
import java.io.File

interface TimelapseRepository {

    fun saveInformation(file: File, timelapseType: TimelapseType)

}