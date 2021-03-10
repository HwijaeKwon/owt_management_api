package develop.management.handler

import develop.management.domain.document.Service
import develop.management.domain.dto.ServiceConfig
import develop.management.domain.dto.ServiceInfo
import develop.management.service.ServiceService
import develop.management.util.error.*
import develop.management.validator.ServiceConfigValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

/**
 * Service 관련된 요청을 처리하는 handler function 모음
 */
@Component
class ServiceHandler(private val serviceService: ServiceService) {
    private final val logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * 새로운 service를 db에 생성한다
     */
    @Operation(
            operationId = "createService",
            description = "Create service",
            requestBody = RequestBody(content = [Content(mediaType = "application/json", schema = Schema(implementation = ServiceConfig::class, required = true))]),
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = ServiceInfo::class))]),
                ApiResponse(responseCode = "400", description = "Bad request error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun create(request: ServerRequest): ServerResponse {
        val validator = ServiceConfigValidator()

        val serviceConfig = try { request.awaitBodyOrNull<ServiceConfig>() } catch (e: Exception) { null } ?: run {
            val error = BadRequestError("Invalid request body: Required arguments must not be null")
            logger.info("Create service fail. Invalid request body: Required arguments must not be null")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val errors = BeanPropertyBindingResult(serviceConfig, ServiceConfig::class.java.name)
        validator.validate(serviceConfig, errors)
        if(errors.allErrors.isNotEmpty()) {
            var message = "Invalid request body: "
            errors.allErrors.forEach { error -> message += error.defaultMessage + " "}
            val error = BadRequestError(message)
            logger.info("Create service fail. Invalid request body: $message")
            return ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }

        val service = Service.create(serviceConfig)

        return try{
            val result = serviceService.create(service)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(ServiceInfo(result.getId(), result.getName(), result.getKey()))
        } catch (e: IllegalStateException) {
            val message = e.message?: "Service already exists"
            val error = AppError("Create service fail: $message")
            logger.info("Create service fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 특정 service를 반환한다
     */
    @Operation(
            operationId = "findOneService",
            description = "find one service",
            parameters = [Parameter(name = "serviceId", description = "Service id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", schema = Schema(implementation = ServiceInfo::class))]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = NotFoundError.exampleService)])]),
            ]
    )
    suspend fun findOne(request: ServerRequest): ServerResponse {
        val streamId = request.pathVariable("serviceId")

        return try {
            val result = serviceService.findOne(streamId)
            ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(ServiceInfo(result.getId(), result.getName(), result.getKey()))
        } catch (e: IllegalArgumentException) {
            val error = NotFoundError("Service not found")
            logger.info("Find one service fail")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }

    /**
     * 모든 service를 반환한다
     */
    @Operation(
            operationId = "findAllService",
            description = "find all services",
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ServiceInfo::class)))]),
            ]
    )
    suspend fun findAll(request: ServerRequest): ServerResponse {
        return ok().contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(serviceService.findAll().map { ServiceInfo(it.getId(), it.getName(), it.getKey()) })
    }

    /**
     * 특정 service를 제거한다
     */
    @Operation(
            operationId = "deleteService",
            description = "Delete service",
            parameters = [Parameter(name = "serviceId", description = "Service id", required = true)],
            responses = [
                ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "text_plain", schema = Schema(implementation = String::class), examples = [ExampleObject(value = "Service deleted")])]),
                ApiResponse(responseCode = "404", description = "Not found", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = BadRequestError.example)])]),
                ApiResponse(responseCode = "500", description = "App error", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorFoam::class), examples = [ExampleObject(value = AppError.example)])]),
            ]
    )
    suspend fun delete(request: ServerRequest): ServerResponse {
        val serviceId = request.pathVariable("serviceId")
        return try {
            if(serviceService.delete(serviceId).deletedCount < 1) {
                logger.info("Delete service fail: Service not found")
                throw IllegalArgumentException("Service not found")
            }
            ok().contentType(MediaType.TEXT_PLAIN).bodyValueAndAwait("Service deleted")
        } catch (e: IllegalStateException) {
            val message = e.message?: ""
            val error = AppError("Delete service fail: $message")
            logger.info("Delete service fail: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        } catch (e: IllegalArgumentException) {
            val message = e.message?: ""
            val error = NotFoundError(message)
            logger.info("Delete service fail. Not found: $message")
            ServerResponse.status(error.status).contentType(MediaType.APPLICATION_JSON).bodyValueAndAwait(error.errorBody)
        }
    }
}