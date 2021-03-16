package develop.management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
class DevelopApplication

fun main(args: Array<String>) {
	runApplication<DevelopApplication>(*args)
}

