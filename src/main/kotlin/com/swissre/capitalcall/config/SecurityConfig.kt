package com.swissre.capitalcall.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
	@Value("\${azure.tenant-id}") private val tenantId: String,
	@Value("\${azure.api-client-id}") private val apiClientId: String
) {

	@Bean
	fun filterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf { it.disable() }
			.cors { it.configurationSource(corsConfigurationSource()) }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { authorize ->
				authorize
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.requestMatchers("/actuator/health").permitAll()
					.anyRequest().authenticated()
			}
			.oauth2ResourceServer { oauth2 ->
				oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter(RolesClaimJwtAuthenticationConverter()) }
			}
		return http.build()
	}

	@Bean
	fun jwtDecoder(): JwtDecoder {
		val issuer = "https://login.microsoftonline.com/$tenantId/v2.0"
		// withJwkSetUri does not hit the network at construction time (unlike
		// fromOidcIssuerLocation), so the app still starts cleanly before Entra ID is configured.
		val jwkSetUri = "https://login.microsoftonline.com/$tenantId/discovery/v2.0/keys"
		val decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()

		val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
		val withAudience = AudienceValidator(apiClientId)
		decoder.setJwtValidator(DelegatingOAuth2TokenValidator(withIssuer, withAudience))
		return decoder
	}

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val config = CorsConfiguration()
		config.allowedOrigins = listOf("http://localhost:4200")
		config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
		config.allowedHeaders = listOf("Authorization", "Content-Type", "Accept-Language")

		val source = UrlBasedCorsConfigurationSource()
		source.registerCorsConfiguration("/**", config)
		return source
	}
}
