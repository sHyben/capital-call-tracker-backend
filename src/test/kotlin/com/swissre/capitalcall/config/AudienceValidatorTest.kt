package com.swissre.capitalcall.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant

class AudienceValidatorTest {

	private val validator = AudienceValidator("expected-client-id")

	private fun jwtWithAudience(audience: List<String>): Jwt =
		Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.claim("aud", audience)
			.audience(audience)
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(60))
			.build()

	@Test
	fun `succeeds when token audience contains the api client id`() {
		val result = validator.validate(jwtWithAudience(listOf("expected-client-id")))
		assertFalse(result.hasErrors())
	}

	@Test
	fun `fails when token audience is a different client id`() {
		val result = validator.validate(jwtWithAudience(listOf("some-other-app-client-id")))
		assertTrue(result.hasErrors())
	}
}
