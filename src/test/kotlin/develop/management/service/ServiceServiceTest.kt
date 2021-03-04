package develop.management.service

import com.google.gson.GsonBuilder
import develop.management.domain.document.Room
import develop.management.domain.document.Service
import develop.management.domain.dto.CreateOptions
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceConfig
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.util.cipher.Cipher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * ServiceData Service 테스트 클래스
 * Authenticator, authorization, validation error는 고려하지 않는다
 * -> Authenticator, authorization, vadlidation error는 따로 테스트 클래스를 만든다
 */
@SpringBootTest
internal class ServiceServiceTest {

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var serviceService: ServiceService

    private lateinit var service: Service

    @BeforeEach
    fun init() = runBlocking {
        val roomName = "room_service_test_room@" + Cipher.generateByteArray(1).decodeToString()
        val options = CreateOptions(roomName)
        val room = Room.create(RoomConfig(roomName, options)).let { roomRepository.save(it) }
        val serviceName = "service_test_service@" + Cipher.generateByteArray(1).decodeToString()
        if(serviceRepository.existsByName(serviceName)) throw AssertionError("Service already exists")
        val serviceData = Service.create(ServiceConfig(serviceName, "key"))
        serviceData.addRoom(room.getId())
        service = serviceRepository.save(serviceData)
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
     * 새로운 service 생성 요청 테스트
     * @expected: DB에 service가 생성되고, 생성된 service를 반환받는다
     */

    @Test
    fun createTest() {
        val serviceName = "create_test_service@" + Cipher.generateByteArray(1).decodeToString()
        if( runBlocking { serviceRepository.existsByName(serviceName) } ) throw AssertionError("Service already exists")
        val serviceConfig = ServiceConfig(serviceName, "key")
        val serviceData = Service.create(serviceConfig)
        val result = runBlocking { serviceService.create(serviceData) }
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        if(!runBlocking { serviceRepository.existsByName(serviceName) }) throw AssertionError("Create service fail")
    }

    /**
     * 이미 존재하는 service 생성 요청 테스트
     * @expected: IllegalArgumentException("Service already exists")
     */
    @Test
    fun duplicatedServiceCreateTest() {
        val serviceConfig = ServiceConfig(service.getName(), "key")
        val serviceData = Service.create(serviceConfig)
        val exception = Assertions.assertThrows(IllegalStateException::class.java) { runBlocking { serviceService.create(serviceData) } }
        Assertions.assertEquals("Service already exists", exception.message)
    }

    /**
     * 개별 service 조회 요청 테스트
     * @expected: 조회한 service를 반환한다
     */
    @Test
    fun findOneTest() {
        val result = runBlocking { serviceService.findOne(service.getId()) }
        Assertions.assertNotNull(result)
        val gson = GsonBuilder().setPrettyPrinting().create()
        println(gson.toJson(result))
        Assertions.assertEquals(service.getId(), result!!.getId())
    }

    /**
     * 존재하지 않는 service 조회 요청 테스트
     * @expected: null을 반환한다
     */
    @Test
    fun findNotExistOneTest() {
        val serviceId = "not_exist_service@" + Cipher.generateByteArray(1).decodeToString()
        if( runBlocking { serviceRepository.existsById(serviceId) } ) throw AssertionError("Service already exists")
        val result = runBlocking { serviceService.findOne(serviceId) }
        Assertions.assertNull(result)
    }

    /**
     * 모든 service 조회 요청 테스트
     * @expected: service 리스트를 반환한다
     */
    @Test
    fun findAllTest() {
        val result = runBlocking { serviceService.findAll() }
        Assertions.assertNotNull(result)
        val size = runBlocking { serviceRepository.findAll() }.size
        Assertions.assertEquals(size, result.size)
    }

    /**
     * service 삭제 요청 테스트
     * @expected: Void?
     */
    @Test
    fun deleteTest() {
        val result = runBlocking { serviceService.delete(service.getId()) }
        Assertions.assertNotNull(result)
        if( runBlocking { serviceRepository.existsById(service.getId()) } ) AssertionError("Service still exists")
        val rooms = runBlocking { roomRepository.findByIds(service.getRooms()) }
        if(rooms.isNotEmpty()) AssertionError("Room still exists")
    }

   /**
     * 존재하지 않는 service 삭제 요청 테스트
     * @expected: IllegalArgumentException("Service not found")
     */
    @Test
    fun deleteNotExistTest() {
       val serviceId = "not_exist_service_test@" + Cipher.generateByteArray(1).decodeToString()
       runBlocking {
           if (serviceRepository.existsById(serviceId)) throw AssertionError("Service already exists")
           val evaluation = Assertions.assertThrows(IllegalArgumentException::class.java) { runBlocking { serviceService.delete(serviceId) } }
           Assertions.assertEquals("Service not found", evaluation.message)
       }
    }
}