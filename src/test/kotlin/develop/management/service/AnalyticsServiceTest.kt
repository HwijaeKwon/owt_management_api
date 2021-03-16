package develop.management.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import develop.management.domain.MediaSubOptions
import develop.management.domain.VideoFormat
import develop.management.domain.dto.Analytics
import develop.management.domain.dto.AnalyticsRequest
import develop.management.rpc.RpcService
import develop.management.rpc.RpcServiceResult
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [AnalyticsService::class, RpcService::class])
internal class AnalyticsServiceTest {

    @Autowired
    private lateinit var analyticsService: AnalyticsService

    @MockBean
    private lateinit var rpcService: RpcService

    /**
     * analytics 생성 테스트
     */
    @Test
    fun createTest() = runBlocking {
        val analyticsRequest = AnalyticsRequest("algorithm", MediaSubOptions(true, true))

        val analytics =
            Analytics("id", Analytics.Analytics("algorithm"), Analytics.Media(VideoFormat("codec", null), "from"))
        whenever(rpcService.addServerSideSubscription(eq("roomId"), any())).thenReturn(
            RpcServiceResult(
                "success",
                Gson().toJson(analytics)
            )
        )

        val result = analyticsService.create("roomId", analyticsRequest)

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()

        println(gson.toJson(result.toString()))

        assertThat(result.id).isEqualTo("id")

        return@runBlocking
    }

    @Test
    fun findAllTest() = runBlocking {
        val analytics =
            Analytics("id", Analytics.Analytics("algorithm"), Analytics.Media(VideoFormat("codec", null), "from"))
        val analytics2 =
            Analytics("id2", Analytics.Analytics("algorithm"), Analytics.Media(VideoFormat("codec", null), "from"))

        val jsonAnalytics = JSONObject(Gson().toJson(analytics))
        val jsonAnalytics2 = JSONObject(Gson().toJson(analytics2))

        val jsonArray = JSONArray()
        jsonArray.put(jsonAnalytics)
        jsonArray.put(jsonAnalytics2)

        whenever(rpcService.getSubscriptionsInRoom("roomId", "analytics")).thenReturn(
            RpcServiceResult(
                "success",
                jsonArray.toString()
            )
        )

        val result = analyticsService.findAll("roomId")

        assertThat(result).isNotNull

        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.toString()))

        assertThat(result.size).isEqualTo(2)

        return@runBlocking
    }

    /**
     * analytics 삭제 테스트
     */
    @Test
    fun deleteTest() = runBlocking {
        whenever(rpcService.deleteSubscription("roomId", "analyticsId", "analytics")).thenReturn(RpcServiceResult("success", "Success"))

        val result = analyticsService.delete("roomId", "analyticsId")
        assertThat(result).isNotNull

        return@runBlocking
    }
}