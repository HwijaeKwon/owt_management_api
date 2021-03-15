package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.MediaInfo
import develop.management.domain.SipOutMedia
import develop.management.domain.dto.MediaOutControlInfo
import develop.management.domain.dto.SipCall
import develop.management.domain.dto.SipCallRequest
import develop.management.domain.dto.StreamInfo
import develop.management.rpc.RpcService
import develop.management.rpc.RpcServiceResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [SipCallService::class, RpcService::class])
internal class SipCallServiceTest {

    @Autowired
    private lateinit var sipCallService: SipCallService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * sip call create 테스트
     */
    @Test
    fun createTest() = runBlocking {
        val sipCallRequest = SipCallRequest("peerURI", SipCallRequest.MediaIn(true, true), SipOutMedia(SipOutMedia.SipOutAudio("audio"), true))

        val sipCall = SipCall("sipId", "type", "peerId",
                StreamInfo("id", "type", MediaInfo(true, true), true),
                SipCall.Output("id", SipOutMedia(SipOutMedia.SipOutAudio("from"), true)))

        whenever(rpcService.addSipCall("roomId", sipCallRequest)).thenReturn(RpcServiceResult("success", Gson().toJson(sipCall).toString()))

        val result = sipCallService.create("roomId", sipCallRequest)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("sipId")

        return@runBlocking
    }

    /**
     * sip call findAll 테스트
     */
    @Test
    fun findAllTest() = runBlocking {
        val sipCall = SipCall("sipId", "type", "peerId",
                StreamInfo("id", "type", MediaInfo(true, true), true),
                SipCall.Output("id", SipOutMedia(SipOutMedia.SipOutAudio("from"), true)))

        val sipCall2 = SipCall("sipId2", "type", "peerId",
                StreamInfo("id", "type", MediaInfo(true, true), true),
                SipCall.Output("id", SipOutMedia(SipOutMedia.SipOutAudio("from"), true)))

        val jsonArray = JSONArray()
        jsonArray.put(JSONObject(Gson().toJson(sipCall)))
        jsonArray.put(JSONObject(Gson().toJson(sipCall2)))

        whenever(rpcService.getSipCallsInRoom("roomId")).thenReturn(RpcServiceResult("success", jsonArray.toString()))

        val result = sipCallService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     * sip call update 테스트
     */
    @Test
    fun updateTest() = runBlocking {
        val sipCall = SipCall("sipId", "type", "peerId",
                StreamInfo("id", "type", MediaInfo(true, true), true),
                SipCall.Output("id", SipOutMedia(SipOutMedia.SipOutAudio("from"), true)))

        val cmds = listOf(MediaOutControlInfo("replace", "path", true))

        whenever(rpcService.updateSipCall("roomId", "sipId", cmds)).thenReturn(RpcServiceResult("success", Gson().toJson(sipCall).toString()))

        val result = sipCallService.update("roomId", "sipId", cmds)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))

        assertThat(result.id).isEqualTo("sipId")

        return@runBlocking
    }

    /**
     * sip call delete 테스트
     */
    @Test
    fun deleteTest() = runBlocking {

        whenever(rpcService.deleteSipCall("roomId", "sipId")).thenReturn(RpcServiceResult("success", "Success"))

        val result = sipCallService.delete("roomId", "sipId")

        assertThat(result).isNotNull

        return@runBlocking
    }
}