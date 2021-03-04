package develop.management.service

import com.google.gson.Gson
import develop.management.domain.dto.StreamInfo
import develop.management.domain.dto.StreamUpdate
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 * stream 관련 비즈니스 로직을 수행하는 서비스
 */
@Service
class StreamService {

    /**
     * v1.1 stream을 v1 stream으로 전환하는 함수
     */
    fun convertToV1Stream(stream: String): String {
        val streamJson = JSONObject(stream)
        return try {
            val videoInfo: JSONObject = streamJson.getJSONObject("media").getJSONObject("video")
            val original: JSONObject = videoInfo.getJSONArray("original").getJSONObject(0)
            videoInfo.put("format", original.get("format"))
            videoInfo.put("parameters", original.get("parameters"))
            videoInfo.remove("original")
            streamJson.toString()
        } catch(e: JSONException) {
            stream
        }
    }

    /**
     * 특정 room에 속한 특정 stream을 반환한다
     */
    suspend fun findOne(roomId: String, streamId: String): StreamInfo? {
        //Todo: rabbitmq로 conference agent에게 stream 목록을 받아와야함
        //rabbitmq에서 flux로 넘겨줄 경우 (test용 json)
        val testStr = """
            {
            "id" = "test"
            }
        """.trimIndent()
        val testJson = JSONObject(testStr)
        val streamFluxList: Flux<JSONObject> = ArrayList<JSONObject>(listOf(testJson)).toFlux()

        //Todo : rabbitmq에서 데이터를 받다가 실패한 경우를 exception error로 catch할 수 있어야 한다
        return streamFluxList.filter { streamJson -> streamJson.getString("id") == streamId }
                .map { streamJson -> convertToV1Stream(streamJson.toString()) }
                .map { streamString -> Gson().fromJson(streamString, StreamInfo::class.java)}
                .awaitSingleOrNull()
    }

    /**
     * 특정 room의 모든 stream을 반환한다
     * Flow를 반환하려고 하였으나, response type이 List여야하므로 List를 반환하도록 구현하였다
     * Flow로 반환한다면 handler에서 list를 만들어줘야 한다
     */
    suspend fun findAll(roomId: String): List<StreamInfo> {
        //Todo: rabbitmq로 conference agent에게 stream 목록을 받아와야함
        //rabbitmq에서 flux로 넘겨줄 경우 (test용 json)
        val testStr = """
            {
            "id" = "test"
            }
        """.trimIndent()
        val testStr2 = """
            {
            "id" = "test2"
            }
        """.trimIndent()
        val testJson = JSONObject(testStr)
        val testJson2 = JSONObject(testStr2)
        val streamFluxList: Flux<JSONObject> = listOf(testJson, testJson2).toFlux()
        //Todo : rabbitmq에서 데이터를 받다가 실패한 경우를 exception error로 catch할 수 있어야 한다
        val streamJson: List<JSONObject> = streamFluxList.collectList().awaitLast()
        return streamJson.map { jsonObject -> convertToV1Stream(jsonObject.toString()) }
                .map { streamString -> Gson().fromJson(streamString, StreamInfo::class.java) }
    }

    /**
     * 특정 room에 속한 특정 stream을 updateInfo를 반영하여 갱신한다
     */
    suspend fun update(roomId: String, streamId: String, updateInfo: StreamUpdate):StreamInfo? {
        //Todo: updateInfo를 validation해야 한다
        //Todo: rabbitmq에 stream update를 요청한다
        //rabbitmq에서 결과를 flux로 넘겨줄 경우 (test용 json)
        val testStr = """
            {
            "id" = "test"
            }
        """.trimIndent()
        val stream = JSONObject(testStr).toMono()

        //Todo : rabbitmq에서 데이터를 받다가 실패한 경우를 exception error로 catch할 수 있어야 한다
        return stream
                .map{ jsonObject -> convertToV1Stream(jsonObject.toString()) }
                .map { streamString -> Gson().fromJson(streamString, StreamInfo::class.java) }
                .awaitSingleOrNull()
    }

    /**
     * 특정 room에 속한 특정 stream을 제거한다
     */
    suspend fun delete(roomId: String, streamId: String) {
        //Todo : rabbitmq에서 요청을 했다가 실패한 경우를 exception error로 catch할 수 있어야 한다
        //제거 요청
    }
}