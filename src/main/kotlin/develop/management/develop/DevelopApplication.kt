package develop.management.develop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevelopApplication

fun main(args: Array<String>) {
	runApplication<DevelopApplication>(*args)
}
