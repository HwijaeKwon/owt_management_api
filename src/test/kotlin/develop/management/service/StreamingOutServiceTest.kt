package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.HlsParameters
import develop.management.domain.MediaSubOptions
import develop.management.domain.OutMedia
import develop.management.domain.dto.StreamingOut
import develop.management.domain.dto.StreamingOutRequest
import develop.management.domain.dto.SubscriptionControlInfo
import develop.management.rpc.RpcService
import develop.management.rpc.RpcServiceResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [StreamingOutService::class, RpcService::class])
internal class StreamingOutServiceTest {

    @Autowired
    private lateinit var streamingOutService: StreamingOutService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * streamingOut 생성 테스트
     */
    @Test
    fun createTest() = runBlocking {
        val streamingOutRequest = StreamingOutRequest("rtmp", "url", HlsParameters("method", 1, 2), MediaSubOptions(true, true))

        val streamingOut = StreamingOut("id", OutMedia(true, true), "rtsp", HlsParameters("method", 1, 2))
        whenever(rpcService.addServerSideSubscription(eq("roomId"), any())).thenReturn(RpcServiceResult("success", Gson().toJson(streamingOut)))

        val result = streamingOutService.create("roomId", streamingOutRequest)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("id")

        return@runBlocking
    }

    /**
     * 모든 streamingOut 조회 테스트
     */
    @Test
    fun findAllTest() = runBlocking {
        val streamingOut = StreamingOut("id", OutMedia(true, true), "rtsp", HlsParameters("method", 1, 2))
        val streamingOut2 = StreamingOut("id2", OutMedia(true, true), "rtsp", HlsParameters("method", 1, 2))

        val jsonArray = JSONArray()
        jsonArray.put(JSONObject(Gson().toJson(streamingOut)))
        jsonArray.put(JSONObject(Gson().toJson(streamingOut2)))

        whenever(rpcService.getSubscriptionsInRoom("roomId", "streaming")).thenReturn(RpcServiceResult("success", jsonArray.toString()))

        val result = streamingOutService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     * streamingOut 업데이트 테스트
     */
    @Test
    fun updateTest() = runBlocking {

        val cmds = listOf(SubscriptionControlInfo("replace", "path", true))

        val streamingOut = StreamingOut("id", OutMedia(true, true), "protocol", true)
        val jsonStreamingOut = Gson().toJson(streamingOut).toString()

        whenever(rpcService.controlSubscription("roomId", "streamingOutId", cmds)).thenReturn(RpcServiceResult("success", jsonStreamingOut))

        val result = streamingOutService.update("roomId", "streamingOutId", cmds)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("id")

        return@runBlocking
    }

    /**
     * streamingOut 삭제 테스트
     */
    @Test
    fun deleteTest() = runBlocking {

        whenever(rpcService.deleteSubscription("roomId", "streamingOutId", "streaming")).thenReturn(RpcServiceResult("success", "Success"))

        val result = streamingOutService.delete("roomId", "streamingOutId")

        assertThat(result).isNotNull

        return@runBlocking
    }
}