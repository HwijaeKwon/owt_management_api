package develop.management.rpc

import com.google.gson.Gson
import develop.management.domain.document.Room
import develop.management.domain.document.Token
import develop.management.domain.dto.*
import kotlinx.coroutines.reactive.awaitSingle
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class RpcService(private val rpcController: RpcController, private val environment: Environment) {

    private val clusterName = environment.getProperty("cluster.name", "owt-cluster")

    suspend fun schedulePortal(tokenCode: String, origin: Token.Origin): RpcServiceResult {
        val args = JSONArray()
        args.put("portal")
        args.put(tokenCode)
        args.put(JSONObject(Gson().toJson(origin)))
        args.put(60*1000)

        //Todo: retry 로직을 넣어야 한다
        val (stream, corrID) = rpcController.sendMessage(clusterName, "schedule", args)
        return try {
            val (data, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(data == "error") return RpcServiceResult("error", error)
            val jsonData = JSONObject(data)
            if(jsonData.isNull("info")) RpcServiceResult("error", "Info not found") else RpcServiceResult("success", jsonData.getJSONObject("info").toString())
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    private suspend fun scheduleRoomController(roomId: String): RpcServiceResult {
        val args = JSONArray()
        args.put("conference")
        args.put(roomId)
        args.put("preference")
        args.put(30*1000)
        val (stream, corrID) = rpcController.sendMessage(clusterName, "schedule", args)
        return try {
            val (data, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(data == "error") return RpcServiceResult("error", error)
            val jsonData = JSONObject(data)
            if(jsonData.isNull("id")) return RpcServiceResult("error", "Id not found")
            val id = jsonData.getString("id").toString()
            val nextArgs = JSONArray()
            val jsonObject = JSONObject()
            jsonObject.put("room", roomId)
            jsonObject.put("task", roomId)
            nextArgs.put(jsonObject)
            val (nextStream, _) = rpcController.sendMessage(id, "getNode", args, corrID)
            val (controller, nextError) = nextStream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(controller == "error") RpcServiceResult("error", nextError) else RpcServiceResult("success", controller)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    private suspend fun getRoomController(roomId: String): RpcServiceResult {
        val args = JSONArray()
        args.put("conference")
        args.put(roomId)
        val (stream, corrID) = rpcController.sendMessage(clusterName, "getScheduled", args)
        return try {
            val (agent, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(agent == "error") return RpcServiceResult("error", error)
            val (nextStream, _) = rpcController.sendMessage(agent, "queryNode", JSONArray().put(roomId))
            val (node, err) = nextStream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(node == "error") return RpcServiceResult("error", err) else RpcServiceResult("success", node)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteRoom(roomId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "destroy", JSONArray())
        return try {
            val (result, _) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            RpcServiceResult("success", result)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getParticipantsInRoom(roomId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "getParticipants", JSONArray())
        return try {
            val (participant, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(participant == "error") RpcServiceResult("error", error) else RpcServiceResult("success", participant)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun updateParticipant(roomId: String, participantId: String, updates: List<PermissionUpdate>): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)
        val args = JSONArray()
        args.put(participantId)
        args.put(JSONArray(Gson().toJson(updates)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlParticipant", args)
        return try {
            val (participant, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(participant == "error") RpcServiceResult("error", error) else RpcServiceResult("success", participant)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteParticipant(roomId: String, participant: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)
        val args = JSONArray()
        args.put(participant)
        val (stream, corrID) = rpcController.sendMessage(controller, "dropParticipant", args)
        return try {
            val (result, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") RpcServiceResult("error", error) else RpcServiceResult("success", result)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getStreamsInRoom(roomId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "getStreams", JSONArray())
        return try {
            val (streams, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(streams == "error") RpcServiceResult("error", error) else RpcServiceResult("success", streams)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun addStreamingIn(roomId: String, pubReq: StreamingInRequest): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        val (nextStatus, nextController) = if(status == "error") {
            if(controller == "Room is inactive") scheduleRoomController(roomId)
            else return RpcServiceResult("error", controller)
        } else RpcServiceResult(status, controller)

        if(nextStatus == "error") return RpcServiceResult("error", nextController)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(pubReq)))
        val (stream, corrID) = rpcController.sendMessage(nextController, "addStreamingIn", args)
        return try {
            val (streamingIn, error) = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(streamingIn == "error") RpcServiceResult("error", error) else RpcServiceResult("success", streamingIn)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun controlStream(roomId: String, streamId: String, cmds: List<StreamUpdate>): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val args = JSONArray()
        args.put(streamId)
        args.put(JSONArray(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlStream", args)
        return try {
            val (data, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(data == "error") RpcServiceResult("error", error) else RpcServiceResult("success", data)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteStream(roomId: String, streamId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "deleteStream", JSONArray().put(streamId))
        return try {
            val (data, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(data == "error") RpcServiceResult("error", error) else RpcServiceResult("success", data)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getSubscriptionsInRoom(roomId: String, type: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        if(type != "streaming" && type != "recording" && type != "webrtc" && type != "analytics")
            return RpcServiceResult("error", "Invalid type")
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "getSubscriptions", JSONArray().put(type))
        return try {
            val (subscription, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(subscription == "error") RpcServiceResult("error", error) else RpcServiceResult("success", subscription)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    //subReq가 다양한 형태로 들어오므로 string로 받도록 한다
    suspend fun addServerSideSubscription(roomId: String, subReq: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        val (nextStatus, nextController) = if(status == "error") {
            if(controller == "Room is inactive") scheduleRoomController(roomId)
            else return RpcServiceResult("error", controller)
        } else RpcServiceResult(status, controller)

        if(nextStatus == "error") return RpcServiceResult("error", nextController)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(subReq)))
        val (stream, corrID) = rpcController.sendMessage(nextController, "addServerSideSubscription", args)
        return try {
            val (subscription, error) = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(subscription == "error") RpcServiceResult("error", error) else RpcServiceResult("success", subscription)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun controlSubscription(roomId: String, subId: String, cmds: List<SubscriptionControlInfo>): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val args = JSONArray()
        args.put(subId)
        args.put(JSONArray(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlSubscription", args)
        return try {
            val (subscription, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(subscription == "error") RpcServiceResult("error", error) else RpcServiceResult("success", subscription)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteSubscription(roomId: String, subId: String, type: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val args = JSONArray()
        args.put(subId)
        args.put(type)
        val (stream, corrID) = rpcController.sendMessage(controller, "deleteSubscription", args)
        return try {
            val (subscription, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(subscription == "error") RpcServiceResult("error", error) else RpcServiceResult("success", subscription)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getSipCallsInRoom(roomId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "getSipCalls", JSONArray())
        return try {
            val (sipCalls, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(sipCalls == "error") RpcServiceResult("error", error) else RpcServiceResult("success", sipCalls)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun addSipCall(roomId: String, options: SipCallRequest): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        val (nextStatus, nextController) = if(status == "error") {
            if(controller == "Room is inactive") scheduleRoomController(roomId)
            else return RpcServiceResult("error", controller)
        } else RpcServiceResult(status, controller)

        if(nextStatus == "error") return RpcServiceResult("error", nextController)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(options)))
        val (stream, corrID) = rpcController.sendMessage(nextController, "makeSipCall", args)
        return try {
            val (sipCall, error) = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(sipCall == "error") RpcServiceResult("error", error) else RpcServiceResult("success", sipCall)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun updateSipCall(roomId: String, sipCallId: String, cmds: List<MediaOutControlInfo>): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val args = JSONArray()
        args.put(sipCallId)
        args.put(JSONArray(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlSipCall", args)
        return try {
            val (sipCall, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(sipCall == "error") RpcServiceResult("error", error) else RpcServiceResult("success", sipCall)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteSipCall(roomId: String, sipCallId: String): RpcServiceResult {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return RpcServiceResult("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "endSipCall", JSONArray().put(sipCallId))
        return try {
            val (result, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") RpcServiceResult("error", error) else RpcServiceResult("success", result)
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun notifySipPortal(changeType: String, room: Room): RpcServiceResult {
        val args = JSONObject()
        args.put("type", changeType)
        args.put("room_id", room.getId())
        args.put("sip", room.getSip())

        val (stream, corrID) = rpcController.sendMessage("sip-portal", "handleSipUpdate", JSONArray().put(args))
        return try {
            val (result, error) = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") RpcServiceResult("error", error) else RpcServiceResult("success", "Success")
        } catch (e: TimeoutException) {
            RpcServiceResult("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }
}