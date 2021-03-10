package develop.management.service

import com.google.gson.Gson
import develop.management.domain.dto.*
import develop.management.rpc.RpcService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service

/**
 * SipCall 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class SipCallService(private val rpcService: RpcService) {

    /**
     * 새로운 sipcall을 생성한다
     */
    suspend fun create(roomId: String, sipCallRequest: SipCallRequest): SipCall {

        val (status, result) = rpcService.addSipCall(roomId, sipCallRequest)
        if(status == "error") throw IllegalStateException("Add sipcall fail. $result")

        return Gson().fromJson(result, SipCall::class.java)
    }

    /**
     * 특정 방의 모든 sipcall을 조회한다
     */
    suspend fun findAll(roomId: String): List<SipCall> {
        val (status, result) = rpcService.getSipCallsInRoom(roomId)
        if(status == "error") throw IllegalStateException("Find all sipcalls fail. $result")

        val jsonArray = JSONArray(result)
        val sipcallList = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (true) {
                sipcallList.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return sipcallList.map { jsonObject -> Gson().fromJson(jsonObject.toString(), SipCall::class.java) }
    }

    /**
     * 특정 room의 sipcall을 업데이트 한다
     */
    suspend fun update(roomId: String, sipCallId: String, cmds: List<MediaOutControlInfo>): SipCall {
        val (status, result) = rpcService.updateSipCall(roomId, sipCallId, cmds)
        if(status == "error") throw IllegalStateException("Update sipcall fail. $result")

        return Gson().fromJson(result, SipCall::class.java)
    }

    /**
     * 특정 room의 특정 sipcall을 제거한다
     */
    suspend fun delete(roomId: String, sipCallId: String) {
        val (status, result) = rpcService.deleteSipCall(roomId, sipCallId)
        if(status == "error") throw IllegalStateException("Delete sipcall fail. $result")
    }
}