package develop.management.router

import develop.management.auth.ServiceAuthenticator
import develop.management.auth.ServiceAuthorizer
import develop.management.handler.RoomHandler
import develop.management.handler.ServiceHandler
import develop.management.validator.RoomValidator
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfig {

    @Autowired
    private lateinit var serviceHandler: ServiceHandler

    @Autowired
    private lateinit var roomHandler: RoomHandler

    @Autowired
    private lateinit var serviceAuthenticator: ServiceAuthenticator

    @Autowired
    private lateinit var serviceAuthorizer: ServiceAuthorizer

    @Autowired
    private lateinit var roomValidator: RoomValidator

    /**
     * room 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/v1/rooms/{roomId}", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = RoomHandler::class, beanMethod = "findOne"),
            RouterOperation(path = "/v1/rooms/{roomId}", method = [RequestMethod.PUT], headers = ["Authorization"], beanClass = RoomHandler::class, beanMethod = "update"),
            RouterOperation(path = "/v1/rooms/{roomId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = RoomHandler::class, beanMethod = "delete"),
            RouterOperation(path = "/v1/rooms", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = RoomHandler::class, beanMethod = "findAll"),
            RouterOperation(path = "/v1/rooms", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = RoomHandler::class, beanMethod = "create"),
    )
    fun roomRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{roomId}".nest {
                    GET("", roomHandler::findOne)
                    PUT("", roomHandler::update)
                    DELETE("", roomHandler::delete)
                    filter(roomValidator::validate)
                }
                GET("", roomHandler::findAll)
                POST("", roomHandler::create)
            }
            filter(serviceAuthenticator::authenticate)
        }
    }

    /**
     * service 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/services/{serviceId}", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = ServiceHandler::class, beanMethod = "findOne"),
            RouterOperation(path = "/services/{serviceId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = ServiceHandler::class, beanMethod = "delete"),
            RouterOperation(path = "/services", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = ServiceHandler::class, beanMethod = "findAll"),
            RouterOperation(path = "/services", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = ServiceHandler::class, beanMethod = "create"),
    )
    fun serviceRouter(): RouterFunction<ServerResponse> = coRouter {
        "/services".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "{serviceId}".nest {
                    GET("", serviceHandler::findOne)
                    DELETE("", serviceHandler::delete)
                }
                GET("", serviceHandler::findAll)
                POST("", serviceHandler::create)
            }
            filter(serviceAuthenticator::authenticate)
            filter(serviceAuthorizer::serviceAuthorize)
        }
    }
}