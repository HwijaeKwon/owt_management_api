package develop.management.domain.enum

import develop.management.domain.*

/**
 * room의 default 값들을 정의한 enum class
 */
enum class Room {

    DEFAULT_PASSWORD {
        override fun get(): String {
            return ""
        }
    },
    DEFAULT_PARTICIPANTLIMIT {
        override fun get(): Number {
            //unlimited: -1
            return -1
        }
    },
    DEFAULT_TIMELIMIT {
        override fun get(): Number {
            //minutes
            //unlimited: -1
            return -1
        }
    },
    DEFAULT_INPUTLIMIT {
        override fun get(): Number {
            //unlimited: -1
            return -1
        }
    },
    DEFAULT_ROLES {
        override fun get(): List<Role> {
            return listOf()
        }
    },
    DEFAULT_VIEWS {
        override fun get(): List<View> {
            //default label
            val label = "common"

            //default viewAudio
            val viewAudio = ViewAudio(AudioFormat("opus", 48000, 2), true)

            //default viewVideo
            val viewVideo = ViewVideo(
                VideoFormat("vp8", null),
                ViewVideo.Parameters(Resolution(640, 480), 24, null,100),
                16,
                ViewVideo.BgColor(0, 0, 0),
                0.8,
                false,
                ViewVideo.Layout("letterbox", null, ViewVideo.Layout.Templates("fluid", ArrayList()))
            )

            return listOf(View(label, viewAudio, viewVideo))
        }
    },
    DEFAULT_MEDIAIN {
        override fun get(): MediaIn {
            return MediaIn(listOf(), listOf())
        }
    },
    DEFAULT_MEDIAOUT {
        override fun get(): MediaOut {
            return MediaOut(listOf(), MediaOut.Video(listOf(), MediaOut.Video.Parameters(listOf(), listOf(), listOf(), listOf())))
        }
    },
    DEFAULT_TRANSCODING {
        override fun get(): Transcoding {
            val video = Transcoding.Video(true, Transcoding.Video.Parameters(resolution = true, framerate = true, bitrate = true, keyFrameInterval = true))
            return Transcoding(true, video)
        }
    },
    DEFAULT_NOTIFYING {
        override fun get(): Notifying {
            return Notifying(participantActivities = true, streamChange = true)
        }
    },
    DEFAULT_SIP {
        override fun get(): Sip {
            return Sip(null, null, null)
        }
    };

    abstract fun get(): Any
}