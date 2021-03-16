package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.MediaInfo
import develop.management.domain.Region
import develop.management.domain.dto.*
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
import java.lang.IllegalArgumentException

@ActiveProfiles("test")
@SpringBootTest(classes = [StreamService::class, RpcService::class])
internal class StreamServiceTest {

    @Autowired
    private lateinit var streamService: StreamService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * stream 조회 테스트
     */
    @Test
    fun findOneTest() = runBlocking {
        val streamInfo = StreamInfo("streamId", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val streamInfo2 = StreamInfo("streamId2", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(Gson().toJson(streamInfo)))
        jsonResult.put(JSONObject(Gson().toJson(streamInfo2)))

        whenever(rpcService.getStreamsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val result = streamService.findOne("roomId", "streamId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("streamId")

        return@runBlocking
    }

    /**
     * 존재하지 않는 방에 대한 stream 조회 테스트
     */
    @Test
    fun findOneInNotExistRoomTest() = runBlocking {

        whenever(rpcService.getStreamsInRoom("roomId")).thenReturn(RpcServiceResult("error", "Room not found"))

        val exception = assertThrows(IllegalStateException::class.java) { runBlocking { streamService.findOne("roomId", "streamId") } }

        assertThat(exception.message).isEqualTo("Get streams in room fail. Room not found")

        return@runBlocking
    }

    /**
     * 존재하지 않는 stream 조회 테스트
     */
    @Test
    fun findNotExistOneTest() = runBlocking {
        val streamInfo = StreamInfo("streamId", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val streamInfo2 = StreamInfo("streamId2", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(Gson().toJson(streamInfo)))
        jsonResult.put(JSONObject(Gson().toJson(streamInfo2)))

        whenever(rpcService.getStreamsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val exception = assertThrows(IllegalArgumentException::class.java) { runBlocking { streamService.findOne("roomId", "streamId3") } }

        assertThat(exception.message).isEqualTo("Stream not found")

        return@runBlocking
    }


    /**
     *  모든 stream 조회 테스트
     */
    @Test
    fun findAllTest() = runBlocking {
        val streamInfo = StreamInfo("streamId", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val streamInfo2 = StreamInfo("streamId2", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))
        val jsonResult = JSONArray()
        jsonResult.put(JSONObject(Gson().toJson(streamInfo)))
        jsonResult.put(JSONObject(Gson().toJson(streamInfo2)))

        whenever(rpcService.getStreamsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonResult.toString()))

        val result = streamService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     *  stream 업데이트 테스트
     */
    @Test
    fun updateTest() = runBlocking {
        val streamUpdate = StreamUpdate("replace", "path", true)
        val streamInfo = StreamInfo("streamId", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))

        whenever(rpcService.controlStream("roomId", "streamId", listOf(streamUpdate))).thenReturn(RpcServiceResult("success", JSONObject(Gson().toJson(streamInfo)).toString()))

        val result = streamService.update("roomId", "streamId", listOf(streamUpdate))

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        return@runBlocking
    }

    /**
     *  stream 삭제 테스트
     */
    @Test
    fun deleteTest() = runBlocking {

        whenever(rpcService.deleteStream("roomId", "streamId")).thenReturn(RpcServiceResult("success", "Success"))

        val result = streamService.delete("roomId", "streamId")

        assertThat(result).isNotNull

        return@runBlocking
    }


    /**
     *  addStreamingIn 테스트
     */
    @Test
    fun addStreamingInTest() = runBlocking {

        val pubReq = StreamingInRequest(StreamingInRequest.Connection("url", "tcp", 8182), StreamingInRequest.Media(true, true), "type")

        val streamInfo = StreamInfo("streamId", "forward", MediaInfo(true, true), MixedInfo("label", listOf(Layout("stream", Region("id", "shape", Region.Area(1,1,1,1))))))

        whenever(rpcService.addStreamingIn("roomId", pubReq)).thenReturn(RpcServiceResult("success", JSONObject(Gson().toJson(streamInfo)).toString()))

        val result = streamService.addStreamingIn("roomId", pubReq)

        assertThat(result).isNotNull

        return@runBlocking
    }
}