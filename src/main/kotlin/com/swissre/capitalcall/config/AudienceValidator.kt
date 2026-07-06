package com.swissre.capitalcall.config

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Entra ID issues tokens tenant-wide; without this check any app registration's token
 * in the tenant would pass issuer validation. This enforces the token was actually
 * requested for THIS API's client ID.
 */
class AudienceValidator(private val apiClientId: String) : OAuth2TokenValidator<Jwt> {

	override fun validate(token: Jwt): OAuth2TokenValidatorResult {
		return if (token.audience.contains(apiClientId)) {
			OAuth2TokenValidatorResult.success()
		} else {
			OAuth2TokenValidatorResult.failure(
				OAuth2Error(
					"invalid_token",
					"The required audience '$apiClientId' is missing from the token",
					null
				)
			)
		}
	}
}
