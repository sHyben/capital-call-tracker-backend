package com.swissre.capitalcall.web

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

	@ExceptionHandler(DomainException::class)
	fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
		val message = messageSource.getMessage(ex.messageKey, ex.args, LocaleContextHolder.getLocale())
		return ResponseEntity.status(ex.status).body(ErrorResponse(ex.status.value(), message))
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
		val locale = LocaleContextHolder.getLocale()
		val fieldErrors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "") }
		val message = messageSource.getMessage("error.validation.failed", null, locale)
		return ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, fieldErrors))
	}

	@ExceptionHandler(AccessDeniedException::class)
	fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
		val locale = LocaleContextHolder.getLocale()
		val message = messageSource.getMessage("error.access.denied", null, locale)
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse(HttpStatus.FORBIDDEN.value(), message))
	}

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
		val locale = LocaleContextHolder.getLocale()
		val message = messageSource.getMessage("error.unexpected", null, locale)
		return ResponseEntity.internalServerError().body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message))
	}
}
