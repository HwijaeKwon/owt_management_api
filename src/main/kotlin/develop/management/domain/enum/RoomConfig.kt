package develop.management.domain.enum

import develop.management.domain.*

/**
 * room configuration의 default 값들을 정의한 enum class
 */
enum class RoomConfig {

    DEFAULT_CONFIG_PARTICIPANTLIMIT
    {
        override fun get(): Number {
            return 10
        }
    },
    DEFAULT_CONFIG_INPUTLIMIT {
        override fun get(): Number {
            //unlimited: -1
            return -1
        }
    },
    DEFAULT_CONFIG_ROLES {
        override fun get(): List<Role> {
            val presenter = Role("presenter", Role.Publish(video = true, audio = true), Role.Subscribe(video = true, audio = true))
            val viewer = Role("viewer", Role.Publish(video = false, audio = false), Role.Subscribe(video = true, audio = true))
            val audioOnlyPresenter = Role("audio_only_presenter", Role.Publish(video = false, audio = true), Role.Subscribe(video = false, audio = true))
            val videoOnlyViewer = Role("video_only_viewer", Role.Publish(video = false, audio = false), Role.Subscribe(video = true, audio = false))
            val sip = Role("sip", Role.Publish(video = true, audio = true), Role.Subscribe(video = true, audio = true))
            return listOf(presenter, viewer, audioOnlyPresenter, videoOnlyViewer, sip)
        }
    },
    DEFAULT_CONFIG_VIEWS {
        override fun get(): List<View> {
            //default label
            val commonLabel = "common"

            //default viewAudio
            val commonViewAudio = ViewAudio(AudioFormat("opus", 48000, 2), false)

            //default viewVideo
            val commonViewVideo = ViewVideo(
                VideoFormat("vp8", null),
                ViewVideo.Parameters(Resolution(1280, 720), 24, null,100),
                200,
                ViewVideo.BgColor(0, 0, 0),
                0.8,
                false,
                ViewVideo.Layout("letterbox", null, ViewVideo.Layout.Templates("fluid", ArrayList()))
            )

            //default label
            val gridLabel = "grid"

            //default viewAudio
            val gridViewAudio = ViewAudio(AudioFormat("opus", 48000, 2), false)

            //default viewVideo
            val gridViewVideo = ViewVideo(
                VideoFormat("vp8", null),
                ViewVideo.Parameters(Resolution(1280, 720), 24, null,30),
                16,
                ViewVideo.BgColor(0, 0, 0),
                0.8,
                false,
                ViewVideo.Layout("letterbox", null, ViewVideo.Layout.Templates("fluid", ArrayList()))
            )

            return listOf(View(commonLabel, commonViewAudio, commonViewVideo), View(gridLabel, gridViewAudio, gridViewVideo))
        }
    },
    DEFAULT_CONFIG_MEDIAIN {
        override fun get(): MediaIn {
            //default audio format list
            val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_IN.get() as List<AudioFormat>

            //default video format list
            val videoFormatList = Video.DEFAULT_CONFIG_VIDEO_IN.get() as List<VideoFormat>

            return MediaIn(audioFormatList, videoFormatList)
        }
    },
    DEFAULT_CONFIG_MEDIAOUT {
        override fun get(): MediaOut {
            //default audio format list
            val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_OUT.get() as List<AudioFormat>

            //default video format list
            val videoFormatList = Video.DEFAULT_CONFIG_VIDEO_OUT.get() as List<VideoFormat>

            //default video parameters
            val videoParameters = Video.DEFAULT_CONFIG_VIDEO_PARA.get() as MediaOut.Video.Parameters

            val video = MediaOut.Video(videoFormatList, videoParameters)

            return MediaOut(audioFormatList, video)
        }
    },
    DEFAULT_CONFIG_TRANSCODING {
        override fun get(): Transcoding {
            val video = Transcoding.Video(true, Transcoding.Video.Parameters(resolution = true, framerate = true, bitrate = true, keyFrameInterval = true))
            return Transcoding(true, video)
        }
    },
    DEFAULT_CONFIG_NOTIFYING {
        override fun get(): Notifying {
            return Notifying(participantActivities = true, streamChange = true)
        }
    },
    DEFAULT_CONFIG_SIP {
        override fun get(): Sip {
            return Sip(null, null, null)
        }
    };

    abstract fun get(): Any
}