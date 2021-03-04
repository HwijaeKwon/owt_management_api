package develop.management

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import develop.management.domain.*
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.*
import develop.management.domain.enum.Audio
import develop.management.domain.enum.Video
import develop.management.repository.KeyRepository
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.util.cipher.Cipher
import develop.management.util.error.ErrorBody
import kotlinx.coroutines.runBlocking
import net.minidev.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.*

/**
 * Room 관련 요청 통합 테스트 클래스
 */
@SpringBootTest(classes = [DevelopApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomIntegrationTest {
    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var keyRepository: KeyRepository

    @Autowired
    private lateinit var initializer: ManagementInitializer

    @LocalServerPort
    private var port: Int = 0

    private lateinit var serviceAuth: String

    private lateinit var superAuth: String

    private lateinit var service: Service

    private lateinit var room: Room

    @BeforeEach
    fun init() = runBlocking {
        initializer.init()
        serviceRepository.findById(initializer.getSuperServiceId())?: throw AssertionError("SuperService dose not exist")

        val roomName = "room_integration_test@" + Cipher.generateByteArray(1).decodeToString()
        val options = CreateOptions()
        room = Room.create(RoomConfig(roomName, options))
        room = roomRepository.save(room)

        val serviceName = "room_integration_test@" + Cipher.generateByteArray(1).decodeToString()
        val key = Cipher.generateKey(128, "HmacSHA256")
        serviceRepository.findByName(serviceName).firstOrNull()?.run { throw AssertionError("Service already exists") }
        service = serviceRepository.save(Service.create(ServiceConfig(serviceName, key)))
        service.also { service.addRoom(room.getId()) }.also { serviceRepository.save(it) }

        val cnonce = "cnonce"
        val timestamp = Date().toString()
        val message = "$timestamp,$cnonce"
        val signature = Cipher.createHmac(message, key, "HmacSHA256")
        serviceAuth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + service.getId() + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + signature

        val superSignature = Cipher.createHmac(message, initializer.getSuperServiceKey(), "HmacSHA256")
        superAuth = "MAuth realm=http://marte3.dit.upm.es," +
                "mauth_signature_method=HMAC_SHA256," +
                "mauth_serviceid=" + initializer.getSuperServiceId() + "," +
                "mauth_cnonce=" + cnonce + "," +
                "mauth_timestamp=" + timestamp + "," +
                "mauth_signature=" + superSignature

        return@runBlocking
    }

    @AfterEach
    fun close() = runBlocking {
        serviceRepository.deleteAll()
        roomRepository.deleteAll()
        keyRepository.deleteAll()
        return@runBlocking
    }

    /** 1. Authentication 테스트 **/
    //Authentication unit test


    /** 2. Authorization 테스트 **/
    //Authentication unit test

    /** 3. Validator 테스트 **/

    /**
     * 서비스에 존재하지 않는 room에 대한 조회 요청 테스트
     * @expected: NotFoundError("Room not found") (status: 404, error_code: 1003)
     */
    @Test
    fun findNotInServiceRoomTest() {
        service.removeRoom(room.getId())
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * Service에 존재하지 않는 room에 대한 삭제 요청 테스트
     * @expected: NotFoundError("Room not found") (status: 404, error_code: 1003)
     */
    @Test
    fun deleteNotInServiceRoom() {
        service.removeRoom(room.getId())
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * Service에 존재하지 않는 room에 대한 업데이트 요청 테스트
     * @expected: NotFoundError("Room not found") (status: 404, error_code: 1003)
     */
    @Test
    fun updateNotInServiceRoom() {
        val update = UpdateOptions()
        service.removeRoom(room.getId())
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(update)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * 존재하지 않는 room에 대한 조회 요청 테스트
     * @expected: Room not found (status: 404, error_code: 1003)
     */
    @Test
    fun findNotExistRoomTest() {
        val roomId = "not_exist_room@" + Cipher.generateByteArray(1).decodeToString()
        runBlocking { roomRepository.findById(roomId) }?. run { throw AssertionError("Room already exists") }
        service.addRoom(roomId)
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/v1/rooms/{roomId}", roomId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * 존재하지 않는 room에 대한 삭제 요청 테스트
     * @expected: Room not found (status: 404, error_code: 1003)
     */
    @Test
    fun deleteNotExistRoomTest() {
        val roomId = "not_exist_room@" + Cipher.generateByteArray(1).decodeToString()
        runBlocking { roomRepository.findById(roomId) }?. run { throw AssertionError("Room already exists") }
        service.addRoom(roomId)
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/v1/rooms/{roomId}", roomId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * 존재하지 않는 room에 대한 업데이트 요청 테스트
     * @expected: Room not found (status: 404, error_code: 1003)
     */
    @Test
    fun updateNotExistRoomTest() {
        val update = UpdateOptions()
        val roomId = "not_exist_room@" + Cipher.generateByteArray(1).decodeToString()
        runBlocking { roomRepository.findById(roomId) }?. run { throw AssertionError("Room already exists") }
        service.addRoom(roomId)
        service = runBlocking { serviceRepository.save(service) }
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", roomId)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(update)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(404, status)
        Assertions.assertEquals(1003, code)
        Assertions.assertEquals("Room not found", message)
    }

    /**
     * RoomConfig를 전달하지 않았을 때 room 생성 요청 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithNoRoomConfigTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
    }

    /**
     * 잘못된 RoomConfig를 전달했을때 room 생성 요청 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithWrongRoomConfigTest() {
        val roomConfig = "test"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid." ,message)
    }

    /**
     * RoomConfig의 options가 null일 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithNoOptionsRoomConfigTest() {
        val roomConfig = JSONObject()
        roomConfig["name"] = "name"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid." ,message)
    }

    /**
     * RoomConfig의 name이 null일 때 roomConfig validator 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithNoNameRoomConfigTest() {
        val roomConfig = JSONObject()
        roomConfig["options"] = JSONObject()

        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid." ,message)
    }

    /**
     * RoomConfig의 name이 empty일 때 roomConfig validator 테스트
     * @expected: Invalid request body: The name must not be empty. (status: 400, error_code: 1201)
     */
    @Test
    fun createWithEmptyNameRoomConfigTest() {
        val roomConfig = RoomConfig("", CreateOptions())

        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: The name must not be empty. " ,message)
    }

    /**
     * Update를 전달하지 않았을 때 room 업데이트 요청 테스트
     * @expected: Invalid request body: Request body is not valid. (status: 400, error_code: 1201)
     */
    @Test
    fun updateWithNoUpdateTest() {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
    }

    /**
     * 잘못된 Update를 전달했을때 room 업데이트 요청 테스트
     * @expected: BadRequestError("Invalid request body: Request body is not valid.") (status: 400, error_code: 1201)
     */
    @Test
    fun updateWithWrongUpdateTest() {
        val update = "test"
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(update)
                .exchange()
                .expectStatus()
                .is4xxClientError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(400, status)
        Assertions.assertEquals(1201, code)
        Assertions.assertEquals("Invalid request body: Request body is not valid.", message)
    }

    //Todo: Default 값이 잘 세팅되었는지 확인해야 한다

    /** 4. Service Test **/

    /**
     * 새로운 room 생성을 요청 테스트
     * @expected: DB에 room이 생성되고, 생성된 room을 반환받는다
     */
    @Test
    fun createRoomTest() = runBlocking {
        val options = CreateOptions()
        val roomConfig = RoomConfig("create_test_room", options)

        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoomInfo::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        roomRepository.findById(result.responseBody!!.id)?: throw AssertionError("Room does not exist")
        val service = serviceRepository.findById(service.getId())?: throw AssertionError("Service does not exist")
        if(service.getRooms().none { it == result.responseBody!!.id }) throw AssertionError("Room does not exist in service")
    }

    /**
     * View와 mediaOut이 매치하지 않을 경우 room 생성 요청 테스트
     * @expected: Create room failed: MediaOut conflicts with View Setting (status: 500, error_code: 2001)
     */
    @Test
    fun createMediaOutFailTest() {
        val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_OUT.get() as List<AudioFormat>
        val videoFormatList = listOf(VideoFormat("test", null))
        val videoParameters = Video.DEFAULT_CONFIG_VIDEO_PARA.get() as MediaOut.Video.Parameters
        val video = MediaOut.Video(videoFormatList, videoParameters)
        val mediaOut = MediaOut(audioFormatList, video)
        val options = CreateOptions("create_test_room", 10, -1,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_ROLES.get() as List<Role>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_VIEWS.get() as List<View>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_MEDIAIN.get() as MediaIn,
                mediaOut,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_TRANSCODING.get() as Transcoding,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_NOTIFYING.get() as Notifying,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_SIP.get() as Sip
        )
        val roomConfig = RoomConfig("name", options)

        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .post()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(roomConfig)
                .exchange()
                .expectStatus()
                .is5xxServerError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(500, status)
        Assertions.assertEquals(2001, code)
        Assertions.assertEquals("Create room failed: MediaOut conflicts with View Setting", message)
    }

    //Todo: Rpc 에러 테스트도 해야한다

    /**
     * 모든 room을 조회하는 테스트
     * @expected : DB에 있는 모든 room의 리스트를 받는다
     */
    @Test
    fun findAllTest() = runBlocking {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/v1/rooms")
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList<RoomInfo>()
                .returnResult()

        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val size = serviceRepository.findById(service.getId())?.getRooms()?.size ?: throw AssertionError("Get rooms in service fail")
        Assertions.assertEquals(size, result.responseBody!!.size)
    }

    /**
     * 개별 room 조회 요청 테스트
     * @expected: 조회한 room을 반환한다
     */
    @Test
    fun findOneTest() = runBlocking {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .get()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoomInfo::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val roomId = result.responseBody!!.id
        Assertions.assertEquals(room.getId(), roomId)
        roomRepository.findById(roomId)?: throw AssertionError("Room does not exist")
        return@runBlocking
    }

    /**
     * Room 삭제 요청 테스트
     * @expected: Room deleted 메세지를 반환한다
     */
    @Test
    fun deleteRoomTest() = runBlocking {
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .delete()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()

        Assertions.assertEquals("Room deleted", result.responseBody)
        roomRepository.findById(room.getId())?.run { throw java.lang.AssertionError("room still in db") }
        service = serviceRepository.findById(service.getId())?: throw AssertionError("Get service fail")
        if(service.getRooms().any { it == room.getId() }) throw AssertionError("Room still exists in service")
    }

    /**
     * Room update 요청 테스트
     * @expected: update된 room을 반환한다
     */
    @Test
    fun updateTest() {
        val update = UpdateOptions(room.getName(), 3)
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(update)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoomInfo::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        Assertions.assertEquals(3, result.responseBody!!.participantLimit)
    }

    //Todo: Update default 값 세팅이 잘되는지 확인해야 한다

    /**
     * View와 mediaOut이 매치하지 않을 경우 update 요청 테스트
     * @expected: Update room failed: MediaOut conflicts with View Setting (status: 500, error_code: 2001)
     */
    @Test
    fun updateMediaOutFailTest() {
        val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_OUT.get() as List<AudioFormat>
        val videoFormatList = listOf(VideoFormat("test", null))
        val videoParameters = Video.DEFAULT_CONFIG_VIDEO_PARA.get() as MediaOut.Video.Parameters
        val video = MediaOut.Video(videoFormatList, videoParameters)
        val mediaOut = MediaOut(audioFormatList, video)
        val updates = UpdateOptions("create_test_room",  10, 3,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_ROLES.get() as List<Role>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_VIEWS.get() as List<View>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_MEDIAIN.get() as MediaIn,
                mediaOut,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_TRANSCODING.get() as Transcoding,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_NOTIFYING.get() as Notifying,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_SIP.get() as Sip
        )
        val result = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
                .put()
                .uri("/v1/rooms/{roomId}", room.getId())
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.add("Authorization", serviceAuth)
                }
                .bodyValue(updates)
                .exchange()
                .expectStatus()
                .is5xxServerError
                .expectBody(ErrorBody::class.java)
                .returnResult()

        Assertions.assertNotNull(result.responseBody)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result.responseBody))
        val status = result.status.value()
        val code = result.responseBody!!.error.code
        val message = result.responseBody!!.error.message
        Assertions.assertEquals(500, status)
        Assertions.assertEquals(2001, code)
        Assertions.assertEquals("Update room failed: MediaOut conflicts with View Setting", message)
    }
}

