package develop.management.service

import com.google.gson.GsonBuilder
import develop.management.domain.*
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.domain.dto.UpdateOptions
import develop.management.domain.enum.Audio
import develop.management.domain.enum.Video
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.repository.mongo.MongoRoomRepository
import develop.management.repository.mongo.MongoServiceRepository
import develop.management.repository.mongo.TestReactiveMongoConfig
import develop.management.rpc.RpcService
import develop.management.util.cipher.Cipher
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

/**
 * Room Service 테스트 클래스
 * Authenticator, authorization, validation error는 고려하지 않는다
 * -> Authenticator, authorization, vadlidation error는 따로 테스트 클래스를 만든다
 */
@SpringBootTest(classes = [TestReactiveMongoConfig::class, MongoServiceRepository::class, MongoRoomRepository::class, RoomService::class, RpcService::class])
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
internal class RoomServiceTest {

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var serviceRepository: MongoServiceRepository

    @Autowired
    private lateinit var roomRepository: MongoRoomRepository

    @MockBean
    private lateinit var rpcService: RpcService

    private lateinit var service: Service

    private lateinit var room: Room

    @BeforeEach
    fun init() = runBlocking {
        val serviceName = "room_service_test_service@" + Cipher.generateByteArray(1).decodeToString()
        if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
        service = Service.create(ServiceConfig(serviceName, "key")).let { serviceRepository.save(it) }
        val roomName = "room_service_test_room@" + Cipher.generateByteArray(1).decodeToString()
        val options = CreateOptions()
        room = Room.create(RoomConfig(roomName, options)).let { roomRepository.save(it) }
        service.addRoom(room.getId())
        service = serviceRepository.save(service)
        room = roomRepository.save(room)
        return@runBlocking
    }

    @AfterEach
    fun close() {
        runBlocking {
            serviceRepository.deleteAll()
            roomRepository.deleteAll()
        }
    }

    /**
     * 새로운 normal room 생성 요청 테스트
     * @expected: DB에 room이 생성되고, 생성된 room을 반환받는다
     */
    @Test
    fun createNormalTest() = runBlocking {
        val options = CreateOptions()
        val room = Room.create(RoomConfig("create_test_room", options)).let { roomRepository.save(it) }

        Mockito.`when`(rpcService.notifySipPortal("create", room)).thenReturn(Pair("success", "Success"))

        val result = roomService.create(service.getId(), room)
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        roomRepository.findById(result.getId())?: throw AssertionError("Create room fail")
        if(serviceRepository.findById(service.getId())?.getRooms()?.none { it == result.getId() } ?: throw AssertionError("Service not found")) {
            throw AssertionError("Service does not have the room")
        }
    }

    /**
     * MediaOut과 view가 일치하지 않는 경우 room 생성 요청 테스트
     * @expected: IllegalStateException("MediaOut conflicts with View Setting")
     */
    @Test
    fun checkMediaOutFailTest()= runBlocking {
        val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_OUT.get() as List<AudioFormat>
        val videoFormatList = listOf(VideoFormat("test", null))
        val videoParameters = Video.DEFAULT_CONFIG_VIDEO_PARA.get() as MediaOut.Video.Parameters
        val video = MediaOut.Video(videoFormatList, videoParameters)
        val mediaOut = MediaOut(audioFormatList, video)
        val options = CreateOptions("create_test_room", -1, -1,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_ROLES.get() as List<Role>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_VIEWS.get() as List<View>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_MEDIAIN.get() as MediaIn,
                mediaOut,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_TRANSCODING.get() as Transcoding,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_NOTIFYING.get() as Notifying,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_SIP.get() as Sip
        )
        val room = Room.create(RoomConfig("create_test_room", options)).let { roomRepository.save(it) }

        val exception = Assertions.assertThrows(IllegalStateException::class.java) { runBlocking { roomService.create(service.getId(), room) } }
        Assertions.assertEquals("MediaOut conflicts with View Setting", exception.message)
    }

    //Todo: Rpc 에러 테스트도 해야한다

    /**
     * 존재하지 않는 service에 대한 room 생성 요청 테스트
     * Authorization과정에서 먼저 처리된다
     * @expected: IllegalArgumentException("Service not found")
     */
    @Test
    fun notExistServiceTest() = runBlocking {
        val serviceId = "not_exist_service_id@" + Cipher.generateByteArray(1).decodeToString()
        serviceRepository.findById(serviceId)?. run { throw AssertionError("Service already exists") }
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.create(serviceId, room) } }
        Assertions.assertEquals("Service not found", exception.message)
        roomRepository.findById(room.getId())?. run { AssertionError("Room still exists") }
        return@runBlocking
    }

    /**
     * 개별 room 조회 요청 테스트
     * @expected: 조회한 room이 반환된다
     */
    @Test
    fun findOneTest() {
        val result = runBlocking { roomService.findOne(service.getId(), room.getId()) }
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        Assertions.assertEquals(room.getId(), result.getId())
    }

    /**
     * Service가 존재하지 않을 때 조회 테스트
     * @expected: IllegalArgumentException("Service not found")
     */
    @Test
    fun findNotExistServiceOneTest() = runBlocking {
        serviceRepository.deleteById(service.getId())
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.findOne(service.getId(), room.getId())}}
        Assertions.assertEquals("Service not found", exception.message)
    }

    /**
     * Service에 존재하지 않는 room 조회 요청 테스트
     * @expected: null이 반환된다
     */
    @Test
    fun findNotExistInServiceOneTest() = runBlocking {
        service.removeRoom(room.getId())
        service = serviceRepository.save(service)
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.findOne(service.getId(), room.getId())}}
        Assertions.assertEquals("Room not found", exception.message)
    }

    /**
     * DB에 존재하지 않는 room 조회 요청 테스트
     * @expected: null이 반환된다
     */
    @Test
    fun findNotExistOneTest() {
        val roomId = "not_exist_room@" + Cipher.generateByteArray(1).decodeToString()
        service.addRoom(roomId)
        service = runBlocking { serviceRepository.save(service) }
        if( runBlocking { roomRepository.existsById((roomId))}) throw AssertionError("Room already exists")
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.findOne(service.getId(), room.getId())}}
        Assertions.assertEquals("Room not found", exception.message)
    }

    /**
     * 모든 room에 대한 조회 테스트
     * @expected: room 리스트가 반환된다
     */
    @Test
    fun findAllTest() {
        val result = runBlocking { roomService.findAll(service.getId()) }
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        val size = runBlocking { roomRepository.findAll() }.size
        Assertions.assertEquals(size, result.size)

    }

    /**
     * room 업데이트 요청 테스트
     * @excepted: 업데이트된 room이 반환된다
     */
    @Test
    fun updateTest() = runBlocking {
        val update = UpdateOptions(room.getName(), 3, 3)

        val result = roomService.update(service.getId(), room.getId(), update)
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        Assertions.assertEquals(3, result.getParticipantLimit())
        Assertions.assertEquals(3, result.getInputLimit())
    }

    //Todo: Update default 값 세팅이 잘되는지 확인해야 한다

    /**
     * MediaOut과 view가 일치하지 않는 경우 room 업데이트 요청 테스트
     * @expected: IllegalStateException("MediaOut conflicts with View Setting")
     */
    @Test
    fun checkMediaOutFailUpdateTest()= runBlocking {
        val audioFormatList = Audio.DEFAULT_CONFIG_AUDIO_OUT.get() as List<AudioFormat>
        val videoFormatList = listOf(VideoFormat("test", null))
        val videoParameters = Video.DEFAULT_CONFIG_VIDEO_PARA.get() as MediaOut.Video.Parameters
        val video = MediaOut.Video(videoFormatList, videoParameters)
        val mediaOut = MediaOut(audioFormatList, video)
        val updates = UpdateOptions("create_test_room", -1, -1,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_ROLES.get() as List<Role>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_VIEWS.get() as List<View>,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_MEDIAIN.get() as MediaIn,
                mediaOut,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_TRANSCODING.get() as Transcoding,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_NOTIFYING.get() as Notifying,
                develop.management.domain.enum.RoomConfig.DEFAULT_CONFIG_SIP.get() as Sip
        )
        val exception = Assertions.assertThrows(IllegalStateException::class.java) { runBlocking { roomService.update(service.getId(), room.getId(), updates) } }
        Assertions.assertEquals("MediaOut conflicts with View Setting", exception.message)
    }

    /**
     * 존재하지 않는 service가 있을 때, room 업데이트 요청 테스트
     * @expected: IllegalArgumentException("Service not found")
     */
    @Test
    fun notExistServiceRoomUpdateTest() = runBlocking {
        val testRoom = Room.create(RoomConfig("name", CreateOptions()))
        val id = runBlocking {
            roomRepository.save(testRoom).getId()
        }
        val serviceId = "test_service@" + Cipher.generateByteArray(1).decodeToString()
        if(serviceRepository.existsById(serviceId)) throw AssertionError("Service already exists")
        val update = UpdateOptions("name")
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.update("", id, update) } }
        Assertions.assertEquals("Service not found", exception.message)
    }

    /**
     * Service에 존재하지 않는 room 업데이트 요청 테스트
     * @expected: null
     */
    @Test
    fun notExistInServiceRoomUpdateTest() {
        val testRoom = Room.create(RoomConfig("name", CreateOptions()))
        val id = runBlocking {
            roomRepository.save(testRoom).getId()
        }
        val update = UpdateOptions("name")
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.update("", id, update) } }
        Assertions.assertEquals("Room not found", exception.message)
    }

    /**
     * DB에 존재하지 않는 room 업데이트 요청 테스트
     * @expected: null
     */
    @Test
    fun notExistRoomUpdateTest() {
        val testRoom = Room.create(RoomConfig("name", CreateOptions()))
        val id = runBlocking {
            roomRepository.save(testRoom).also {
                roomRepository.deleteById(it.getId())
            }.getId()
        }
        service.addRoom(id)
        service = runBlocking { serviceRepository.save(service) }
        val update = UpdateOptions("name")
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.update("", id, update) } }
        Assertions.assertEquals("Room not found", exception.message)
    }

    /**
     * room 삭제 요청 테스트
     * @expected: DeleteResult
     */
    @Test
    fun deleteTest() = runBlocking {

        Mockito.`when`(rpcService.deleteRoom(room.getId())).thenReturn(Pair("success", "Success"))

        val result = roomService.delete(service.getId(), room.getId())
        Assertions.assertNotNull(result)
        if(roomRepository.existsById(room.getId())) throw AssertionError("Room still exists")
        Assertions.assertEquals(1, result.deletedCount)
    }

    /**
     * 존재하지 않는 서비스와 함께 room 삭제 요청 테스트
     * @expected: IllegalArgumentException("Service not found")
     */
    @Test
    fun deleteNotExistServiceTest() = runBlocking {
        val serviceId = "test_service@" + Cipher.generateByteArray(1).decodeToString()
        if(serviceRepository.existsById(serviceId)) throw AssertionError("Service already exists")
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.delete(serviceId, room.getId()) } }
        Assertions.assertEquals("Service not found", exception.message)
    }

    /**
     * 서비스에 존재하지 않는 room 삭제 요청 테스트
     * @expected: IllegalArgumentException("Room not found")
     */
    @Test
    fun deleteNotExistInServiceTest() = runBlocking {
        var testRoom = Room.create(RoomConfig("name", CreateOptions()))
        testRoom = roomRepository.save(testRoom)
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.delete(service.getId(), testRoom.getId()) } }
        Assertions.assertEquals("Room not found", exception.message)
    }

    /**
     * DB에 존재하지 않는 room 삭제 요청 테스트
     * @expected: IllegalArgumentException("Room not found")
     */
    @Test
    fun deleteNotExistTest() = runBlocking {
        var testRoom = Room.create(RoomConfig("name", CreateOptions()))
        testRoom = roomRepository.save(testRoom)
        service.addRoom(testRoom.getId())
        serviceRepository.save(service)
        roomRepository.deleteById(testRoom.getId())
        val id = roomRepository.save(testRoom).also { roomRepository.deleteById(it.getId()) }.getId()
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { roomService.delete(service.getId(), id) } }
        Assertions.assertEquals("Room not found", exception.message)
    }
}