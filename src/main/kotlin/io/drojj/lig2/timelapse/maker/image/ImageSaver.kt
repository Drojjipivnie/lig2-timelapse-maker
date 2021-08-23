package io.drojj.lig2.timelapse.maker.image

import io.drojj.lig2.timelapse.maker.TimelapseType

interface ImageSaver {

    fun saveImage(timelapseType: TimelapseType)

}