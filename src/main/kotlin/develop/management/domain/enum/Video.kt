package develop.management.domain.enum

import develop.management.domain.MediaOut
import develop.management.domain.VideoFormat

/**
 * video 관련 데이터의 default 값들을 정의한 enum class
 */
enum class Video {

    DEFAULT_CONFIG_VIDEO_IN {
        override fun get(): List<VideoFormat> {
            return listOf(VideoFormat("h264", null), VideoFormat("vp8", null), VideoFormat("vp9", null))
        }
    },
    DEFAULT_CONFIG_VIDEO_OUT {
        override fun get(): List<VideoFormat> {
            return listOf(VideoFormat("vp8", null), VideoFormat("h264", "CB"), VideoFormat("h264", "B"), VideoFormat("vp9", null))
        }
    },
    DEFAULT_CONFIG_VIDEO_PARA {
        override fun get(): MediaOut.Video.Parameters {
            return MediaOut.Video.Parameters(
                listOf("x3/4", "x2/3", "x1/2", "x1/3", "x1/4"), //resolution
                listOf(6, 12, 15, 24, 30, 48, 60), //framerate
                listOf("x0.8", "x0.6", "x0.4", "x0.2"), //bitrate
                listOf(100, 30, 5, 2, 1) //keyFrameInterval
            )
        }
    };

    abstract fun get(): Any
}