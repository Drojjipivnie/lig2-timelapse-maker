package io.drojj.lig2.timelapse.maker.video

import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener
import org.slf4j.LoggerFactory
import java.text.CharacterIterator
import java.text.StringCharacterIterator

class HumanReadableProgressListener : ProgressListener {
    override fun progress(progress: Progress) {
        LOGGER.info("frame={}; total_size={}, status={}",
            progress.frame,
            bytesToHuman(progress.total_size),
            progress.status
        )
    }

    private fun bytesToHuman(originalBytes: Long): String {
        if (-1000 < originalBytes && originalBytes < 1000) {
            return "$originalBytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kM")
        var bytes = originalBytes
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HumanReadableProgressListener::class.java)
    }

}