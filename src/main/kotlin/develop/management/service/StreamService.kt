package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.dto.StreamInfo
import develop.management.domain.dto.StreamUpdate
import develop.management.domain.dto.StreamingInRequest
import develop.management.rpc.RpcService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service

/**
 * stream 관련 비즈니스 로직을 수행하는 서비스
 */
@Service
class StreamService(private val rpcService: RpcService) {

    private final val gson = Gson()

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
    suspend fun findOne(roomId: String, streamId: String): StreamInfo {
        val (status, streams) = rpcService.getStreamsInRoom(roomId)
        if(status == "error") throw IllegalStateException("Get streams in room fail. $streams")
        val jsonArray = JSONArray(streams)
        val streamArray = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (i < jsonArray.length()) {
                streamArray.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }

        return streamArray.firstOrNull { it.getString("id") == streamId }?.let {
            gson.fromJson(it.toString(), StreamInfo::class.java)
        }?: throw IllegalArgumentException("Stream not found")
    }

    /**
     * 특정 room의 모든 stream을 반환한다
     */
    suspend fun findAll(roomId: String): List<StreamInfo> {
        val (status, streams) = rpcService.getStreamsInRoom(roomId)
        if(status == "error") throw IllegalStateException("Get streams in room fail. $streams")
        val jsonArray = JSONArray(streams)
        val streamArray = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (i < jsonArray.length()) {
                streamArray.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return streamArray.map { jsonObject -> convertToV1Stream(jsonObject.toString()) }
                .map { streamString -> gson.fromJson(streamString, StreamInfo::class.java) }
    }

    /**
     * 특정 room에 속한 특정 stream을 updateInfo를 반영하여 갱신한다
     */
    suspend fun update(roomId: String, streamId: String, updateInfoList: List<StreamUpdate>):StreamInfo {

        val (status, result) = rpcService.controlStream(roomId, streamId, updateInfoList)

        if(status == "error") throw IllegalStateException("Control stream fail. $result")

        val stream = convertToV1Stream(result)
        return gson.fromJson(stream, StreamInfo::class.java)
    }

    /**
     * 특정 room에 속한 특정 stream을 제거한다
     */
    suspend fun delete(roomId: String, streamId: String) {
        val (status, result) = rpcService.deleteStream(roomId, streamId)
        if(status == "error") throw IllegalStateException("Delete stream fail. $result")
    }

    /**
     * 새로운 streaming in을 추가한다
     */
    suspend fun addStreamingIn(roomId: String, pub_req: StreamingInRequest): StreamInfo {
        val (status, result) = rpcService.addStreamingIn(roomId, pub_req)
        if(status == "error") throw IllegalStateException("Add streamingin fail. $result")

        val stream = convertToV1Stream(result)
        return gson.fromJson(stream, StreamInfo::class.java)
    }
}