package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.MediaSubOptions
import develop.management.domain.OutMedia
import develop.management.domain.dto.RecordingRequest
import develop.management.domain.dto.Recordings
import develop.management.domain.dto.SubscriptionControlInfo
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
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [RecordingService::class, RpcService::class])
internal class RecordingServiceTest {

    @Autowired
    private lateinit var recordingService: RecordingService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * recording 생성 테스트
     */
    @Test
    fun createTest() = runBlocking {

        val recordings = Recordings("id", OutMedia(true, true), Recordings.Storage("host", "file"))
        val jsonRecordings = JSONObject(Gson().toJson(recordings))
        whenever(rpcService.addServerSideSubscription(eq("roomId"), any())).thenReturn(RpcServiceResult("success", jsonRecordings.toString()))
        val recordingRequest = RecordingRequest("container", MediaSubOptions(true, true))
        val result = recordingService.create("roomId", recordingRequest)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.toString()))

        assertThat(result.id).isEqualTo("id")

        return@runBlocking
    }

    /**
     * 모든 recording 조회 테스트
     */
    @Test
    fun findAllTest() = runBlocking {
        val recordings = Recordings("id", OutMedia(true, true), Recordings.Storage("host", "file"))
        val recordings2 = Recordings("id2", OutMedia(true, true), Recordings.Storage("host", "file"))

        val jsonArray = JSONArray()
        jsonArray.put(JSONObject(Gson().toJson(recordings)))
        jsonArray.put(JSONObject(Gson().toJson(recordings2)))

        whenever(rpcService.getSubscriptionsInRoom("roomId", "recording")).thenReturn(RpcServiceResult("success", jsonArray.toString()))

        val result = recordingService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.toString()))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     * recording 업데이트 테스트
     */
    @Test
    fun updateTest() = runBlocking {
        val cmds = listOf(SubscriptionControlInfo("replace", "path", true))

        val recordings = Recordings("id", OutMedia(true, true), Recordings.Storage("host", "file"))
        val jsonRecordings = JSONObject(Gson().toJson(recordings))
        whenever(rpcService.controlSubscription("roomId", "recordingsId", cmds)).thenReturn(RpcServiceResult("success", jsonRecordings.toString()))

        val result = recordingService.update("roomId", "recordingsId", cmds)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.toString()))

        assertThat(result.id).isEqualTo("id")

        return@runBlocking
    }

    @Test
    fun deleteTest() = runBlocking {
        whenever(rpcService.deleteSubscription("roomId", "recordingsId", "recording")).thenReturn(RpcServiceResult("success", "Success"))

        val result = recordingService.delete("roomId", "recordingsId")
        assertThat(result).isNotNull

        return@runBlocking
    }
}