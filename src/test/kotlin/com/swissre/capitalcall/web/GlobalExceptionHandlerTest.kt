package com.swissre.capitalcall.web

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import java.math.BigDecimal
import java.util.Locale

class GlobalExceptionHandlerTest {

	private val messageSource = mockk<MessageSource>()
	private val handler = GlobalExceptionHandler(messageSource)

	@Test
	fun `domain exception resolves localized message and its own http status`() {
		every {
			messageSource.getMessage("error.capitalCall.amountExceedsCommitment", any(), any<Locale>())
		} returns "Requested amount 100 exceeds remaining commitment of 50."

		val response = handler.handleDomainException(
			CapitalCallExceedsCommitmentException(BigDecimal("100"), BigDecimal("50"))
		)

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
		assertEquals("Requested amount 100 exceeds remaining commitment of 50.", response.body?.message)
	}

	@Test
	fun `not found exception maps to 404`() {
		every {
			messageSource.getMessage("error.investor.notFound", any(), any<Locale>())
		} returns "Investor with id 7 was not found."

		val response = handler.handleDomainException(InvestorNotFoundException(7))

		assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
		assertEquals(404, response.body?.status)
	}

	@Test
	fun `access denied maps to 403 with localized message`() {
		every {
			messageSource.getMessage("error.access.denied", null, any<Locale>())
		} returns "You do not have permission to perform this action."

		val response = handler.handleAccessDenied(AccessDeniedException("denied"))

		assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
		assertEquals("You do not have permission to perform this action.", response.body?.message)
	}
}
