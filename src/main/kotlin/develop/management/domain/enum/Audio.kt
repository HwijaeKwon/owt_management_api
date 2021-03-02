package develop.management.domain.enum

import develop.management.domain.AudioFormat

/**
 * audio 관련 데이터의 default 값들을 정의한 enum class
 */
enum class Audio {

    DEFAULT_CONFIG_AUDIO_IN {
        override fun get(): List<AudioFormat> {
            return listOf(
                AudioFormat("opus", 48000, 2),
                AudioFormat("isac", 16000, null),
                AudioFormat("isac", 32000, null),
                AudioFormat("g722", 16000, 1),
                //AudioFormat("g722", 16000, 2),
                AudioFormat("pcma", null, null),
                AudioFormat("pcmu", null, null),
                AudioFormat("aac", null, null),
                AudioFormat("ac3", null, null),
                AudioFormat("nellymoser", null, null),
                AudioFormat("ilbc", null, null)
            )
        }
    },
    DEFAULT_CONFIG_AUDIO_OUT {
        override fun get(): List<AudioFormat> {
            return listOf(
                AudioFormat("opus", 48000, 2),
                AudioFormat("isac", 16000, null),
                AudioFormat("isac", 32000, null),
                AudioFormat("g722", 16000, 1),
                //AudioFormat("g722", 16000, 2),
                AudioFormat("pcma", null, null),
                AudioFormat("pcmu", null, null),
                AudioFormat("aac", 48000, 2),
                AudioFormat("ac3", null, null),
                AudioFormat("nellymoser", null, null),
                AudioFormat("ilbc", null, null)
            )
        }
    };

    abstract fun get(): Any
}