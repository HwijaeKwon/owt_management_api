package develop.management.router

import develop.management.auth.ServiceAuthenticator
import develop.management.auth.ServiceAuthorizer
import develop.management.domain.dto.RoomConfig
import develop.management.domain.dto.ServiceInfo
import develop.management.handler.*
import develop.management.util.error.AppError
import develop.management.util.error.BadRequestError
import develop.management.util.error.ErrorFoam
import develop.management.validator.RoomValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.reactive.function.server.*

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
    private lateinit var streamingOutHandler: StreamingOutHandler

    @Autowired
    private lateinit var recordingHandler: RecordingHandler

    @Autowired
    private lateinit var sipCallHandler: SipCallHandler

    @Autowired
    private lateinit var analyticsHandler: AnalyticsHandler

    @Autowired
    private lateinit var serviceAuthenticator: ServiceAuthenticator

    @Autowired
    private lateinit var serviceAuthorizer: ServiceAuthorizer

    @Autowired
    private lateinit var roomValidator: RoomValidator

    @Bean
    @RouterOperations(
            RouterOperation(path = "*", method = [RequestMethod.OPTIONS],
                    operation = Operation(responses = [ApiResponse(responseCode = "200", description = "Success")]))
    )
    fun defaultRouter(): RouterFunction<ServerResponse> = coRouter {
        OPTIONS("*") {
            ServerResponse.ok().bodyValueAndAwait("")
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
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-ins", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "addStreamingIn"),
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-ins/{streamId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = StreamHandler::class, beanMethod = "delete"),
    )
    fun streamRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/streams".nest {
                    "/{streamId}".nest {
                        GET("", streamHandler::findOne)
                        PATCH("", streamHandler::update)
                        DELETE("", streamHandler::delete)
                    }
                    GET("", streamHandler::findAll)
                }
                "/streaming-ins".nest {
                    POST("", streamHandler::addStreamingIn)
                    DELETE("/{streamId}", streamHandler::delete)
                }
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * streaming out 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-outs/{streamId}", method = [RequestMethod.PATCH], headers = ["Authorization"], beanClass = StreamingOutHandler::class, beanMethod = "update"),
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-outs/{streamId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = StreamingOutHandler::class, beanMethod = "delete"),
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-outs", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = StreamingOutHandler::class, beanMethod = "add"),
            RouterOperation(path = "/v1/rooms/{roomId}/streaming-outs", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = StreamingOutHandler::class, beanMethod = "findAll"),
    )
    fun streamingOutRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/streaming-outs".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{streamingOutId}".nest {
                        PATCH("", streamingOutHandler::update)
                        DELETE("", streamingOutHandler::delete)
                }
                GET("", streamingOutHandler::findAll)
                POST("", streamingOutHandler::add)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * recording 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
        RouterOperation(path = "/v1/rooms/{roomId}/recordings/{recordingId}", method = [RequestMethod.PATCH], headers = ["Authorization"], beanClass = RecordingHandler::class, beanMethod = "update"),
        RouterOperation(path = "/v1/rooms/{roomId}/recordings/{recordingId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = RecordingHandler::class, beanMethod = "delete"),
        RouterOperation(path = "/v1/rooms/{roomId}/recordings", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = RecordingHandler::class, beanMethod = "add"),
        RouterOperation(path = "/v1/rooms/{roomId}/recordings", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = RecordingHandler::class, beanMethod = "findAll"),
    )
    fun recordingRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/recordings".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{recordingId}".nest {
                    PATCH("", recordingHandler::update)
                    DELETE("", recordingHandler::delete)
                }
                GET("", recordingHandler::findAll)
                POST("", recordingHandler::add)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * sipcall 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
        RouterOperation(path = "/v1/rooms/{roomId}/sipcalls/{sipCallId}", method = [RequestMethod.PATCH], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "update"),
        RouterOperation(path = "/v1/rooms/{roomId}/sipcalls/{sipCallId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "delete"),
        RouterOperation(path = "/v1/rooms/{roomId}/sipcalls", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "add"),
        RouterOperation(path = "/v1/rooms/{roomId}/sipcalls", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "findAll"),
    )
    fun sipCallRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/sipcalls".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{sipCallId}".nest {
                    PATCH("", sipCallHandler::update)
                    DELETE("", sipCallHandler::delete)
                }
                GET("", sipCallHandler::findAll)
                POST("", sipCallHandler::add)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }

    /**
     * analytics 관련 요청을 처리하는 router functions
     */
    @Bean
    @RouterOperations(
        RouterOperation(path = "/v1/rooms/{roomId}/analytics/{analyticsId}", method = [RequestMethod.DELETE], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "delete"),
        RouterOperation(path = "/v1/rooms/{roomId}/analytics", method = [RequestMethod.POST], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "add"),
        RouterOperation(path = "/v1/rooms/{roomId}/analytics", method = [RequestMethod.GET], headers = ["Authorization"], beanClass = SipCallHandler::class, beanMethod = "findAll"),
    )
    fun analyticsRouter(): RouterFunction<ServerResponse> = coRouter {
        "/v1/rooms/{roomId}/analytics".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                "/{analyticsId}".nest {
                    DELETE("", analyticsHandler::delete)
                }
                GET("", analyticsHandler::findAll)
                POST("", analyticsHandler::add)
            }
            filter(serviceAuthenticator::authenticate)
            filter(roomValidator::validate)
        }
    }
}