package io.drojj.lig2.timelapse.maker.image

import java.awt.image.BufferedImage

interface CameraHandler {

    fun instantImage(): BufferedImage

}