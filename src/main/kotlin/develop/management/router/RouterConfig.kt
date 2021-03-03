package develop.management.router

import develop.management.auth.ServiceAuthenticator
import develop.management.auth.ServiceAuthorizer
import develop.management.handler.*
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
    private lateinit var tokenHandler: TokenHandler

    @Autowired
    private lateinit var participantHandler: ParticipantHandler

    @Autowired
    private lateinit var streamHandler: StreamHandler

    @Autowired
    private lateinit var serviceAuthenticator: ServiceAuthenticator

    @Autowired
    private lateinit var serviceAuthorizer: ServiceAuthorizer

    @Autowired
    private lateinit var roomValidator: RoomValidator

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
     * token 관련 요청을 처리하는 router function
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/v1/rooms/{roomId}/tokens", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = TokenHandler::class, beanMethod = "create"),
    )
    fun tokenRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/tokens".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("", tokenHandler::create)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * participant 관련 요청을 처리하는 router function
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/v1/rooms/{roomId}/participants/{participantId}", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = ParticipantHandler::class, beanMethod = "findOne"),
            RouterOperation(path = "/v1/rooms/{roomId}/participants/{participantId}", method = [RequestMethod.PATCH], headers = ["Authorization"], beanClass = ParticipantHandler::class, beanMethod = "update"),
            RouterOperation(path = "/v1/rooms/{roomId}/participants/{participantId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = ParticipantHandler::class, beanMethod = "delete"),
            RouterOperation(path = "/v1/rooms/{roomId}/participants", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "findAll"),
    )
    fun participantRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/participants".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{participantId}".nest {
                    GET("", participantHandler::findOne)
                    PATCH("", participantHandler::update)
                    DELETE("", participantHandler::delete)
                }
                GET("", participantHandler::findAll)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * stream 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/v1/rooms/{roomId}/streams/{streamId}", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "findOne"),
            RouterOperation(path = "/v1/rooms/{roomId}/streams/{streamId}", method = [RequestMethod.PATCH], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "update"),
            RouterOperation(path = "/v1/rooms/{roomId}/streams/{streamId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "delete"),
            RouterOperation(path = "/v1/rooms/{roomId}/streams", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "findAll"),
    )
    fun streamRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/streams".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{streamId}".nest {
                    GET("", streamHandler::findOne)
                    PATCH("", streamHandler::update)
                    DELETE("", streamHandler::delete)
                }
                GET("", streamHandler::findAll)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }
}