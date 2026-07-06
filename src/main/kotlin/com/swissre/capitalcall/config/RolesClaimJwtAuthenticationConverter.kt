package com.swissre.capitalcall.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * Entra ID app roles arrive in the "roles" claim (array of strings), not "scp" or "groups".
 * Spring's default JwtGrantedAuthoritiesConverter looks at "scope"/"scp" — this replaces it.
 */
class RolesClaimJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

	override fun convert(jwt: Jwt): AbstractAuthenticationToken {
		val roles = jwt.getClaimAsStringList("roles") ?: emptyList()
		val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
		return JwtAuthenticationToken(jwt, authorities)
	}
}
