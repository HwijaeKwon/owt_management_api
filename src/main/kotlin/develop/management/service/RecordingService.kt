package develop.management.service

import com.google.gson.Gson
import develop.management.domain.dto.*
import develop.management.rpc.RpcService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service

/**
 * Recordings 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class RecordingService(private val rpcService: RpcService) {

    /**
     * 새로운 recordings를 생성한다
     */
    suspend fun create(roomId: String, recordingRequest: RecordingRequest): Recordings {
        val subReq = RecordingsSubscriptionRequest()
        subReq.type = "recording"
        subReq.connection.container = recordingRequest.container
        subReq.media = recordingRequest.media

        val (status, result) = rpcService.addServerSideSubscription(roomId, JSONObject(Gson().toJson(subReq)).toString())
        if(status == "error") throw IllegalStateException("Add recording fail. $result")

        return Gson().fromJson(result, Recordings::class.java)
    }

    /**
     * 특정 방의 모든 recordings을 조회한다
     */
    suspend fun findAll(roomId: String): List<Recordings> {
        val (status, result) = rpcService.getSubscriptionsInRoom(roomId, "recording")
        if(status == "error") throw IllegalStateException("Find all recording fail. $result")

        val jsonArray = JSONArray(result)
        val recordingsList = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (true) {
                recordingsList.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return recordingsList.map { jsonObject -> Gson().fromJson(jsonObject.toString(), Recordings::class.java) }
    }

    /**
     * 특정 room의 recordings를 업데이트 한다
     */
    suspend fun update(roomId: String, recordingsId: String, cmds: List<SubscriptionControlInfo>): Recordings {
        val (status, result) = rpcService.controlSubscription(roomId, recordingsId, cmds)
        if(status == "error") throw IllegalStateException("Update recording fail. $result")

        return Gson().fromJson(result, Recordings::class.java)
    }

    /**
     * 특정 room의 특정 recordings를 제거한다
     */
    suspend fun delete(roomId: String, recordingsId: String) {
        val (status, result) = rpcService.deleteSubscription(roomId, recordingsId, "recording")
        if(status == "error") throw IllegalStateException("Delete recording fail. $result")
    }
}