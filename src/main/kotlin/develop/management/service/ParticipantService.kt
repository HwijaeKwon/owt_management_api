package develop.management.service

import com.google.gson.Gson
import develop.management.domain.dto.ParticipantDetail
import develop.management.domain.dto.PermissionUpdate
import develop.management.rpc.RpcService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Service

/**
 * 사용자 관련 비즈니스 로직을 담당하는 service class
 */
@Service
class ParticipantService(private val rpcService: RpcService) {

    private final val gson = Gson()

    /**
     * Conference에서 사용자를 조회한다
     */
    suspend fun findOne(roomId: String, participantId: String): ParticipantDetail {
        val (status, result) = rpcService.getParticipantsInRoom(roomId)
        if(status == "error") {
            throw IllegalStateException("Get participants in room fail. $result")
        }

        val jsonArray = JSONArray(result)
        val participantArray = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (i < jsonArray.length()) {
                participantArray.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return participantArray.firstOrNull { it.getString("id") == participantId }?.let {
            gson.fromJson(it.toString(), ParticipantDetail::class.java)
        }?: throw IllegalArgumentException("Participant not found")
    }

    /**
     * Conference에서 특정 방의 모든 사용자를 조회한다
     */
    suspend fun findAll(roomId: String): List<ParticipantDetail> {
        val (status, result) = rpcService.getParticipantsInRoom(roomId)
        if(status == "error") throw IllegalStateException("Get participants in room fail. $result")

        val jsonArray = JSONArray(result)
        val participantArray = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (i < jsonArray.length()) {
                participantArray.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return participantArray.map { jsonObject -> gson.fromJson(jsonObject.toString(), ParticipantDetail::class.java) }
    }

    /**
     * 특정 room의 사용자 권한을 conference에 업데이트 한다
     */
    suspend fun update(roomId: String, participantId: String, permissionUpdates: List<PermissionUpdate>): ParticipantDetail {
        val (status, result) = rpcService.updateParticipant(roomId, participantId, permissionUpdates)
        if(status == "error") throw IllegalStateException("Update participant fail. $result")

        return gson.fromJson(result, ParticipantDetail::class.java)
    }

    /**
     * Conference에서 특정 room의 특정 participant를 제거한다
     */
    suspend fun delete(roomId: String, participantId: String) {
        val (status, result) = rpcService.deleteParticipant(roomId, participantId)
        if(status == "error") throw IllegalStateException("Delete participant fail. $result")
    }
}