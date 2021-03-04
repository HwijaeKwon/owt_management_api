package develop.management.service

import com.google.gson.Gson
import com.mongodb.client.result.DeleteResult
import develop.management.domain.ViewVideo
import develop.management.domain.document.Room
import develop.management.domain.dto.UpdateOptions
import develop.management.repository.RoomRepository
import develop.management.repository.ServiceRepository
import develop.management.repository.mongo.RetryOperation
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*
import kotlin.collections.ArrayList

/**
 * Room 관련 비즈니스 로직을 수행하는 서비스
 */
@Service
class RoomService(private val serviceRepository: ServiceRepository,
                  private val roomRepository: RoomRepository,
                  private val transactionalOperator: TransactionalOperator,
                  private val retryOperation: RetryOperation
) {

    /**
     * audio만 있는 view의 label을 반환한다
     */
    fun getAudioOnlyLabels(room: Room): List<String> {
        val labels : MutableList<String> = ArrayList()
        room.getViews().filter { it.video == false }.forEach { view -> labels.add(view.label) }
        return labels
    }

    /**
     * mediaOut 설정과 view의 설정이 같은지 확인한다
     */
    fun checkMediaOut(room: Room): Boolean {
        return if(room.getViews().isEmpty()) true //views가 null이거나 빈 리스트일 경우
            else room.getViews().any {
            view -> room.getMediaOut().audio.any {
                it.codec == view.audio.format.codec && it.sampleRate == view.audio.format.sampleRate && it.channelNum == view.audio.format.channelNum
            }.and(room.getMediaOut().video.format.any {
                (view.video == false) || (it.codec == convertToViewVideo(view.video).format.codec && it.profile == convertToViewVideo(view.video).format.profile)
            })
        }
    }

    fun convertToViewVideo(viewVideo: Any): ViewVideo {
        if(viewVideo !is ViewVideo) {
            return Gson().fromJson(viewVideo.toString(), ViewVideo::class.java)
        }
        return viewVideo
    }

    /**
     * Room의 유효성을 검사하고 새로운 room을 db에 생성한다
     * RPC를 통해 conference에 room 생성을 알린다
     */
    suspend fun create(serviceId: String, room: Room): Room {
        if(!checkMediaOut(room)) throw IllegalStateException("MediaOut conflicts with View Setting")

        //Todo: audio only view를 고려해야한다 -> 필요한 부분인지 살펴보자
        //Todo: sip인 경우 rpc로 sip portal에 정보를 전달해야한다
       /* return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                val savedRoom = roomRepository.save(room)
                service.addRoom(savedRoom.getId())
                serviceRepository.save(service)
                return@executeAndAwait savedRoom
            }!!
        }*/

        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        val savedRoom = roomRepository.save(room)
        service.addRoom(savedRoom.getId())
        serviceRepository.save(service)
        return savedRoom
    }

    /**
     * 특정 room을 반환한다
     */
    suspend fun findOne(serviceId: String, roomId: String): Room? {
        /*return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                if(service.getRooms().none{it == roomId}) return@executeAndAwait null
                return@executeAndAwait roomRepository.findById(roomId)
            }
        }*/
        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        if(service.getRooms().none{it == roomId}) return null
        return roomRepository.findById(roomId)
    }

    /**
     * 모든 room을 반환한다
     */
    suspend fun findAll(serviceId: String): List<Room> {
        /*return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                return@executeAndAwait roomRepository.findByIds(service.getRooms())
            }!!
        }*/
        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        return roomRepository.findByIds(service.getRooms())
    }

    /**
     * 업데이트 정보의 유효성을 확인하고 room의 정보를 갱신한다
     */
    suspend fun update(serviceId: String, roomId: String, update: UpdateOptions): Room? {
        /*return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                if(service.getRooms().none{it == roomId}) return@executeAndAwait null
                return@executeAndAwait roomRepository.findById(roomId)?.let {
                    if(!checkMediaOut(it.also { it.update(update) })) throw IllegalStateException("MediaOut conflicts with View Setting")
                    //Todo: updateAudioOnlyViews가 필요한지 살펴보자
                    roomRepository.save(it)
                }
            }
        }*/
        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        if(service.getRooms().none{it == roomId}) return null
        return roomRepository.findById(roomId)?.let {
            if(!checkMediaOut(it.also { it.update(update) })) throw IllegalStateException("MediaOut conflicts with View Setting")
            //Todo: updateAudioOnlyViews가 필요한지 살펴보자
            roomRepository.save(it)
        }
    }

    /**
     * 특정 room을 제거한다
     */
    suspend fun delete(serviceId: String, roomId: String): DeleteResult {
        /*return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                if(service.getRooms().none{it == roomId}) throw IllegalArgumentException("Room not found")
                service.removeRoom(roomId)
                serviceRepository.save(service)
                val result = roomRepository.deleteById(roomId)
                if(result.deletedCount == 0L) throw IllegalArgumentException("Room not found")
                return@executeAndAwait result
            }!!
        }*/
        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        if(service.getRooms().none{it == roomId}) throw IllegalArgumentException("Room not found")
        service.removeRoom(roomId)
        serviceRepository.save(service)
        val result = roomRepository.deleteById(roomId)
        if(result.deletedCount == 0L) throw IllegalArgumentException("Room not found")
        return result
    }
}
