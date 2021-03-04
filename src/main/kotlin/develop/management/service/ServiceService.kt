package develop.management.service

import com.mongodb.client.result.DeleteResult
import develop.management.repository.ServiceRepository
import develop.management.repository.mongo.RetryOperation
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait


/**
 * stream 관련 비즈니스 로직을 수행하는 서비스
 */
@Service
class ServiceService(private val serviceRepository: ServiceRepository, private val transactionalOperator: TransactionalOperator, private val retryOperation: RetryOperation) {

    /**
     * 새로운 service를 db에 생성한다
     */
    suspend fun create(service: develop.management.domain.document.Service): develop.management.domain.document.Service {
       /* return retryOperation.execute {
            println("ServiceDataService.create")
            transactionalOperator.executeAndAwait {
                if(serviceRepository.existsByName(service.getName())) throw IllegalStateException("Service already exists")
                println("save service!!!")
                serviceRepository.save(service)
            }!!
        }*/
        if(serviceRepository.existsByName(service.getName())) throw IllegalStateException("Service already exists")
        println("save service!!!")
        return serviceRepository.save(service)
    }

    /**
     * 특정 service를 반환한다
     */
    suspend fun findOne(serviceId: String): develop.management.domain.document.Service? {
        //repository error 체크가 필요하다 -> service id는 null이 될 수 없다.
        return serviceRepository.findById(serviceId)
    }

    /**
     * 모든 service를 반환한다
     */
    suspend fun findAll(): List<develop.management.domain.document.Service> {
        return serviceRepository.findAll()
    }

    /**
     * 특정 service를 제거한다
     */
    suspend fun delete(serviceId: String): DeleteResult {
        /*return retryOperation.execute {
            transactionalOperator.executeAndAwait {
                val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
                serviceRepository.deleteById(service.getId())
            }!!
        }*/
        val service = serviceRepository.findById(serviceId)?: throw IllegalArgumentException("Service not found")
        return serviceRepository.deleteById(service.getId())
    }
}