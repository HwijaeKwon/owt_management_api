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
class AnalyticsService(private val rpcService: RpcService) {

    /**
     * 새로운 analytics를 생성한다
     */
    suspend fun create(roomId: String, analyticsRequest: AnalyticsRequest): Analytics {
        val subDesc = SubscriptionDescription()
        subDesc.type = "analytics"
        subDesc.connection.algorithm = analyticsRequest.algorithm
        subDesc.media = analyticsRequest.media

        val (status, result) = rpcService.addServerSideSubscription(roomId, JSONObject(Gson().toJson(subDesc)).toString())
        if(status == "error") throw IllegalStateException("Add analytics fail. $result")

        return Gson().fromJson(result, Analytics::class.java)
    }

    /**
     * 특정 방의 모든 analytics를 조회한다
     */
    suspend fun findAll(roomId: String): List<Analytics> {
        val (status, result) = rpcService.getSubscriptionsInRoom(roomId, "analytics")
        if(status == "error") throw IllegalStateException("Find all analytics fail. $result")

        val jsonArray = JSONArray(result)
        val analyticsList = mutableListOf<JSONObject>()
        try {
            var i = 0
            while (true) {
                analyticsList.add(jsonArray.getJSONObject(i))
                i++
            }
        } catch (e: JSONException) {
            //
        }
        return analyticsList.map { jsonObject -> Gson().fromJson(jsonObject.toString(), Analytics::class.java) }
    }

    /**
     * 특정 room의 특정 analytics를 제거한다
     */
    suspend fun delete(roomId: String, analyticsId: String) {
        val (status, result) = rpcService.deleteSubscription(roomId, analyticsId, "analytics")
        if(status == "error") throw IllegalStateException("Delete analytics fail. $result")
    }
}