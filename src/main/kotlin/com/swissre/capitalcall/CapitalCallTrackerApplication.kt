package com.swissre.capitalcall

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CapitalCallTrackerApplication

fun main(args: Array<String>) {
	runApplication<CapitalCallTrackerApplication>(*args)
}
