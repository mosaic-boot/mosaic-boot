package io.mosaicboot.examplewebservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExampleWebServiceApplication

fun main(args: Array<String>) {
    runApplication<ExampleWebServiceApplication>(*args)
}
