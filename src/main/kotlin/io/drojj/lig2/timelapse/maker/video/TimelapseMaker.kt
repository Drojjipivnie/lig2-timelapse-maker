package io.drojj.lig2.timelapse.maker.video

import io.drojj.lig2.timelapse.maker.TimelapseType

interface TimelapseMaker {

    fun makeTimelapse(timelapseType: TimelapseType)

}