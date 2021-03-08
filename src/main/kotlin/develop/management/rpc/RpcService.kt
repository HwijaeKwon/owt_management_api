package develop.management.rpc

import com.google.gson.Gson
import develop.management.domain.document.Room
import develop.management.domain.document.Token
import develop.management.domain.dto.*
import kotlinx.coroutines.reactive.awaitSingle
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.concurrent.TimeoutException

@Service
class RpcService(private val rpcController: RpcController) {

    //Todo: clusterName을 config에서 읽어와야 한다
    private val clusterName = "owt-cluster"

    suspend fun schedulePortal(tokenCode: String, origin: Token.Origin): Pair<String, String> {
        val args = JSONArray()
        args.put("portal")
        args.put(tokenCode)
        args.put(JSONObject(Gson().toJson(origin)))
        //Todo: retry 로직을 넣어야 한다
        val (stream, corrID) = rpcController.sendMessage(clusterName, "schedule", args)
        return try {
            val reply = stream.timeout(Duration.ofSeconds(60)).toMono().awaitSingle()
            if(reply == "error") return Pair("error", "Reply error")
            val jsonReply = JSONObject(reply)
            if(jsonReply.isNull("info")) Pair("error", "Info not found") else Pair("success", jsonReply.getJSONObject("info").toString())
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    private suspend fun scheduleRoomController(roomId: String): Pair<String, String> {
        val args = JSONArray()
        args.put("conference")
        args.put(roomId)
        args.put("preference")
        val (stream, corrID) = rpcController.sendMessage(clusterName, "schedule", args)
        return try {
            val reply = stream.timeout(Duration.ofSeconds(30)).toMono().awaitSingle()
            if(reply == "error") return Pair("error", "Error in scheduling room controller")
            val jsonReply = JSONObject(reply)
            if(jsonReply.isNull("id")) return Pair("error", "Id not found")
            val id = jsonReply.getString("id").toString()
            val nextArgs = JSONArray()
            val jsonObject = JSONObject()
            jsonObject.put("room", roomId)
            jsonObject.put("task", roomId)
            nextArgs.put(jsonObject)
            val (nextStream, nextCorrID) = rpcController.sendMessage(id, "getNode", args, corrID)
            val result = nextStream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Error in scheduling room controller") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    private suspend fun getRoomController(roomId: String): Pair<String, String> {
        val args = JSONArray()
        args.put("conference")
        args.put(roomId)
        val (stream, corrID) = rpcController.sendMessage(clusterName, "getScheduled", args)
        return try {
            val agent = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(agent == "error") return Pair("error", "Room is inactive")
            val (nextStream, nextCorrID) = rpcController.sendMessage(agent, "queryNode", JSONArray().put(roomId))
            val node = nextStream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(node == "error") return Pair("error", "Room is inactive") else Pair("success", node)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteRoom(roomId: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "destroy", JSONArray())
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getParticipantsInRoom(roomId: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "destroy", JSONArray())
        return try {
            val participant = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(participant == "error") Pair("error", "Get participant fail") else Pair("success", participant)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun updateParticipant(roomId: String, participant: String, updates: PermissionUpdate): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)
        val args = JSONArray()
        args.put(participant)
        args.put(JSONObject(Gson().toJson(updates)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlParticipant", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Update participant fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteParticipant(roomId: String, participant: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)
        val args = JSONArray()
        args.put(participant)
        val (stream, corrID) = rpcController.sendMessage(controller, "dropParticipant", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Delete participant fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getStreamsInRoom(roomId: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)
        val (stream, corrID) = rpcController.sendMessage(controller, "getStreams", JSONArray())
        return try {
            val streams = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(streams == "error") Pair("error", "Delete participant fail") else Pair("success", streams)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun addStreamingIn(roomId: String, pubReq: StreamingInRequest): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, result) = getRoomController(roomId)
        val (nextStatus, controller) = if(status == "error") {
            if(result == "Room is inactive") scheduleRoomController(roomId)
            else return Pair("error", result)
        } else Pair(status, result)

        if(nextStatus == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(pubReq)))
        val (stream, corrID) = rpcController.sendMessage(controller, "addStreamingIn", args)
        return try {
            val streams = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(streams == "error") Pair("error", "Add streaming in fail") else Pair("success", streams)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun controlStream(roomId: String, stream: String, cmds: StreamUpdate): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlStream", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Control stream fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteStream(roomId: String, stream: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "deleteStream", JSONArray().put(stream))
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Delete stream fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getSubscriptionsInRoom(roomId: String, type: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        if(type != "streaming" && type != "recording" && type != "webrtc" && type != "analytics") return Pair("error", "Invalid type")
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "controlStream", JSONArray().put(type))
        return try {
            val subscription = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(subscription == "error") Pair("error", "Get subscriptions in room fail") else Pair("success", subscription)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    //subReq가 다양한 형태로 들어오므로 string로 받도록 한다
    suspend fun addServerSideSubscription(roomId: String, subReq: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, result) = getRoomController(roomId)
        val (nextStatus, controller) = if(status == "error") {
            if(result == "Room is inactive") scheduleRoomController(roomId)
            else return Pair("error", result)
        } else Pair(status, result)

        if(nextStatus == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(subReq)))
        val (stream, corrID) = rpcController.sendMessage(controller, "addServerSideSubscription", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Add server side subscription fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun controlSubscription(roomId: String, subId: String, cmds: SubscriptionControlInfo): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(subId)
        args.put(JSONObject(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlSubscription", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Get subscriptions in room fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteSubscription(roomId: String, subId: String, type: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(subId)
        args.put(type)
        val (stream, corrID) = rpcController.sendMessage(controller, "deleteSubscription", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Delete subscription fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun getSipCallsInRoom(roomId: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "getSipCalls", JSONArray())
        return try {
            val sipCalls = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(sipCalls == "error") Pair("error", "Delete subscription fail") else Pair("success", sipCalls)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun addSipCall(roomId: String, options: SipCallRequest): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, result) = getRoomController(roomId)
        val (nextStatus, controller) = if(status == "error") {
            if(result == "Room is inactive") scheduleRoomController(roomId)
            else return Pair("error", result)
        } else Pair(status, result)

        if(nextStatus == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(roomId)
        args.put(JSONObject(Gson().toJson(options)))
        val (stream, corrID) = rpcController.sendMessage(controller, "makeSipCall", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(9)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Add sip call fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun updateSipCall(roomId: String, sipCallId: String, cmds: MediaOutControlInfo): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val args = JSONArray()
        args.put(sipCallId)
        args.put(JSONObject(Gson().toJson(cmds)))
        val (stream, corrID) = rpcController.sendMessage(controller, "controlSipCall", args)
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Update sip call fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun deleteSipCall(roomId: String, sipCallId: String): Pair<String, String> {
        //Validate 과정은 validator에서 수행한다
        val (status, controller) = getRoomController(roomId)
        if(status == "error") return Pair("error", controller)

        val (stream, corrID) = rpcController.sendMessage(controller, "endSipCall", JSONArray().put(sipCallId))
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Delete sip call fail") else Pair("success", result)
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }

    suspend fun notifySipPortal(changeType: String, room: Room): Pair<String, String> {
        val args = JSONObject()
        args.put("type", changeType)
        args.put("room_id", room.getId())
        args.put("sip", room.getSip())

        val (stream, corrID) = rpcController.sendMessage("sip-portal", "handleSipUpdate", JSONArray().put(args))
        return try {
            val result = stream.timeout(Duration.ofSeconds(3)).toMono().awaitSingle()
            if(result == "error") Pair("error", "Notify sip portal fail") else Pair("success", "Success")
        } catch (e: TimeoutException) {
            Pair("error", "Reply timeout")
        } finally {
            rpcController.deleteCorrID(corrID)
        }
    }
}