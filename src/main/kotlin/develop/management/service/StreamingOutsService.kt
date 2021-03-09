package develop.management.service

import com.google.gson.Gson
import develop.management.domain.DashParameters
import develop.management.domain.HlsParameters
import develop.management.domain.dto.*
import develop.management.rpc.RpcService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service

/**
 * 사용자 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class StreamingOutsService(private val rpcService: RpcService) {

    private fun guessProtocol(url: String): String {
        if(url.startsWith("rtmp://")) {
            return "rtmp"
        } else if(url.startsWith("rtsp://")) {
            return "rtsp"
        } else if(url.endsWith(".m3u8")) {
            return "hls"
        } else if(url.endsWith(".mpd")) {
            return "dash"
        } else {
            return "unknown"
        }
    }

    /**
     * 새로운 Streaming out을 생성한다
     */
    suspend fun create(roomId: String, streamingOutRequest: StreamingOutRequest): StreamingOut {
        val subReq = SubscriptionRequest()
        subReq.type = "streaming"
        subReq.connection.protocol = streamingOutRequest.protocol?: guessProtocol(streamingOutRequest.url)
        subReq.connection.url = streamingOutRequest.url
        subReq.media = streamingOutRequest.media

        //Todo: Json string 형태 확인하기
        if(subReq.connection.protocol === "hls") {
            subReq.connection.parameters = streamingOutRequest.parameters?: HlsParameters("PUT", 2, 5)
        } else if(subReq.connection.protocol == "dash") {
            subReq.connection.parameters = streamingOutRequest.parameters?: DashParameters("PUT", 2, 5)
        }

        val (status, result) = rpcService.addServerSideSubscription(roomId, JSONObject(Gson().toJson(subReq)).toString())
        if(status == "error") throw IllegalStateException("Add streaming outs fail. $result")

        return Gson().fromJson(result, StreamingOut::class.java)
    }

    /**
     * 특정 방의 모든 streaming out을 조회한다
     */
    suspend fun findAll(roomId: String): List<StreamingOut> {
        val (status, result) = rpcService.getSubscriptionsInRoom(roomId, "streaming")
        if(status == "error") throw IllegalStateException("Get all streaming outs fail. $result")

        val jsonArray = JSONArray()
        val streamingOutList = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (true) {
                streamingOutList.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return streamingOutList.map { jsonObject -> Gson().fromJson(jsonObject.toString(), StreamingOut::class.java) }
    }

    /**
     * 특정 room의 streaming out을 업데이트 한다
     */
    suspend fun update(roomId: String, streamingOutId: String, cmds: SubscriptionControlInfo): StreamingOut {
        val (status, result) = rpcService.controlSubscription(roomId, streamingOutId, cmds)
        if(status == "error") throw IllegalStateException("Update streaming out fail. $result")

        return Gson().fromJson(result, StreamingOut::class.java)
    }

    /**
     * 특정 room의 특정 streaming out를 제거한다
     */
    suspend fun delete(roomId: String, streamingOutId: String) {
        val (status, result) = rpcService.deleteSubscription(roomId, streamingOutId, "streaming")
        if(status == "error") throw IllegalStateException("Delete streaming out fail. $result")
    }
}