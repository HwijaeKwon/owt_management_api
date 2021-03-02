package develop.management

import develop.management.domain.document.Key
import develop.management.domain.document.Service
import develop.management.domain.dto.ServiceConfig
import develop.management.repository.KeyRepository
import develop.management.repository.ServiceRepository
import develop.management.util.cipher.Cipher
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.util.DefaultPropertiesPersister
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import javax.annotation.PostConstruct


/**
 * management api 초기 세팅을 수행하는 클래스
 */
@Component
class ManagementAPIInitializer(private val serviceRepository: ServiceRepository, private val keyRepository: KeyRepository) {

    private val superServicePath = "src/main/resources/superService.info"

    private val superServiceName: String = "superService"
    private var superServiceId: String = ""
    private var superServiceKey: String = ""

    @PostConstruct
    fun init() {
        runBlocking {
            serviceRepository.findByName(superServiceName).firstOrNull()?.also {
                superServiceId = it.getId()
                superServiceKey = Cipher.decrypt(it.getKey())
                writeProperties(superServiceName, superServiceId, superServiceKey)
            }?: run {
                superServiceKey = Cipher.generateKey(128, "HmacSHA256")
                superServiceId = Service.create(ServiceConfig(superServiceName, superServiceKey)).let {
                    serviceRepository.save(it).getId()
                }
                writeProperties(superServiceName, superServiceId, superServiceKey)
            }

            //Todo: 기존에는 server가 새로 뜰때마다 token 발행용 key를 업데이트하였는데,
            // 현재는 db에 key가 이미 있으면 새로 발행하지 않도록 변경하였다

            if(!keyRepository.existsById(0)) keyRepository.save(Key.createKey())
        }
    }

    fun writeProperties(superServiceName: String, superServiceId: String, superServiceKey: String) {

        var outputStream: OutputStream? = null
        try {
            val props = Properties()
            props.setProperty("rest.superService.name", superServiceName)
            props.setProperty("rest.superService.id", superServiceId)
            props.setProperty("rest.superService.key", superServiceKey)
            outputStream = FileOutputStream(File(superServicePath))
            val propertiesPersister = DefaultPropertiesPersister()
            propertiesPersister.store(props, outputStream, "superService properties")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception(e.message)
        } finally {
            outputStream?.also { it.close() }
        }
    }

    fun getSuperServiceName(): String = this.superServiceName
    fun getSuperServiceId(): String = this.superServiceId
    fun getSuperServiceKey(): String = this.superServiceKey
}