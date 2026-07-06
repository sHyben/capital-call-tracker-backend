package com.swissre.capitalcall.web

import java.time.Instant

data class ErrorResponse(
	val status: Int,
	val message: String,
	val fieldErrors: Map<String, String>? = null,
	val timestamp: Instant = Instant.now()
)
