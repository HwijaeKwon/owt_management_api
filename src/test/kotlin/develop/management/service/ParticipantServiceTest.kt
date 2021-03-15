package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.dto.ParticipantDetail
import develop.management.domain.dto.Permission
import develop.management.domain.dto.PermissionUpdate
import develop.management.rpc.RpcService
import develop.management.rpc.RpcServiceResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.lang.IllegalArgumentException

@SpringBootTest(classes = [ParticipantService::class, RpcService::class])
internal class ParticipantServiceTest {

    @Autowired
    private lateinit var participantService: ParticipantService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * participant 조회 요청 테스트
     */
    @Test
    fun findOneTest() = runBlocking {
        val participantDetail = ParticipantDetail("participantId", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val participantDetail2 = ParticipantDetail("participantId2", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val jsonParticipantDetail = Gson().toJson(participantDetail)
        val jsonParticipantDetail2 = Gson().toJson(participantDetail2)
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(jsonParticipantDetail))
        jsonResult.put(JSONObject(jsonParticipantDetail2))
        whenever(rpcService.getParticipantsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val result = participantService.findOne("roomId", "participantId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("participantId")

        return@runBlocking
    }

    /**
     * 존재하지 않는 room의 participant 조회 요청 테스트
     */
    @Test
    fun findOneInNotExistRoomTest() = runBlocking {
        whenever(rpcService.getParticipantsInRoom("roomId")).thenReturn(RpcServiceResult("error", "Room not found"))

        val exception = assertThrows(IllegalStateException::class.java) { runBlocking { participantService.findOne("roomId", "participantId") } }

        assertThat(exception.message).isEqualTo("Get participants in room fail. Room not found")

        return@runBlocking
    }

    /**
     * 존재하지 않는 participant 조회 요청 테스트
     */
    @Test
    fun findNotExistOneTest() = runBlocking {
        val participantDetail = ParticipantDetail("participantId", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val participantDetail2 = ParticipantDetail("participantId2", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val jsonParticipantDetail = Gson().toJson(participantDetail)
        val jsonParticipantDetail2 = Gson().toJson(participantDetail2)
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(jsonParticipantDetail))
        jsonResult.put(JSONObject(jsonParticipantDetail2))
        whenever(rpcService.getParticipantsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val exception = assertThrows(IllegalArgumentException::class.java) { runBlocking { participantService.findOne("roomId", "participantId3") } }

        assertThat(exception.message).isEqualTo("Participant not found")

        return@runBlocking
    }

    /**
     * 모든 participant 조회 요청 테스트
     */
    @Test
    fun findAllTest() = runBlocking {
        val participantDetail = ParticipantDetail("participantId", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val participantDetail2 = ParticipantDetail("participantId2", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val jsonParticipantDetail = Gson().toJson(participantDetail)
        val jsonParticipantDetail2 = Gson().toJson(participantDetail2)
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(jsonParticipantDetail))
        jsonResult.put(JSONObject(jsonParticipantDetail2))
        whenever(rpcService.getParticipantsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val result = participantService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     * participant 업데이트 요청 테스트
     */
    @Test
    fun updateTest() = runBlocking {
        val permissionUpdate = listOf(PermissionUpdate("replace", "path", true))
        val participantDetail = ParticipantDetail("participantId", "role", "user", Permission(Permission.Publish(true, true), Permission.Subscribe(true ,true)))
        val jsonParticipantDetail = Gson().toJson(participantDetail)
        whenever(rpcService.updateParticipant("roomId", "participantId", permissionUpdate)).thenReturn(RpcServiceResult("success", jsonParticipantDetail.toString()))

        val result = participantService.update("roomId", "participantId", permissionUpdate)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        return@runBlocking
    }

    /**
     * participant 삭제 요청 테스트
     */
    @Test
    fun deleteTest() = runBlocking {
        whenever(rpcService.deleteParticipant("roomId", "participantId")).thenReturn(RpcServiceResult("success", "Success"))

        val result = participantService.delete("roomId", "participantId")

        assertThat(result).isNotNull

        return@runBlocking
    }
}