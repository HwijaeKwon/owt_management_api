package develop.management.common

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Component
class CorsFilter {

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        corsConfig.addAllowedOrigin("*")
        corsConfig.addAllowedMethod(HttpMethod.POST)
        corsConfig.addAllowedMethod(HttpMethod.GET)
        corsConfig.addAllowedMethod(HttpMethod.OPTIONS)
        corsConfig.addAllowedMethod(HttpMethod.DELETE)
        corsConfig.addAllowedMethod(HttpMethod.PATCH)
        corsConfig.addAllowedHeader("origin")
        corsConfig.addAllowedHeader("authorization")
        corsConfig.addAllowedHeader("content-type")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
}