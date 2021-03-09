package develop.management.validator

import com.google.gson.Gson
import develop.management.domain.HlsParameters
import develop.management.domain.MediaSubOptions
import develop.management.domain.ViewVideo
import develop.management.domain.dto.StreamingOutRequest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class StreamingOutRequestValidatorTest {

    @Test
    fun test() {
        val streamOutRequest = StreamingOutRequest("protocol", "url", "parameters", MediaSubOptions(false, false))
        streamOutRequest.parameters = HlsParameters("method", 12, 13)

        println(streamOutRequest.toString())

        val jsonObject = JSONObject(Gson().toJson(streamOutRequest))

        println(jsonObject)

        val jsonStr = jsonObject.toString()

        println(jsonStr)

        val streamOutReq = Gson().fromJson(jsonStr, StreamingOutRequest::class.java)

        if(streamOutReq.parameters !is HlsParameters) {
            val result = Gson().fromJson(streamOutReq.parameters.toString(), HlsParameters::class.java)
            if(result is HlsParameters) {
                println("ok")
            }
        }
    }
}