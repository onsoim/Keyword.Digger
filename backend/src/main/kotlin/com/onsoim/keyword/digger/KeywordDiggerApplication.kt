package com.onsoim.keyword.digger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class KeywordDiggerApplication

fun main(args: Array<String>) {
    runApplication<KeywordDiggerApplication>(*args)
}
