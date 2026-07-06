package com.swissre.capitalcall.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale

/**
 * Locale is driven explicitly by the Accept-Language header (en/sk, default en) —
 * never guessed from the request. Bean validation and domain error messages both
 * resolve against the same messages.properties/messages_sk.properties files.
 */
@Configuration
class LocaleConfig {

	@Bean
	fun localeResolver(): LocaleResolver {
		val resolver = AcceptHeaderLocaleResolver()
		resolver.supportedLocales = listOf(Locale.ENGLISH, Locale("sk"))
		resolver.setDefaultLocale(Locale.ENGLISH)
		return resolver
	}

	@Bean
	fun messageSource(): MessageSource {
		val source = ReloadableResourceBundleMessageSource()
		source.setBasenames("classpath:messages")
		source.setDefaultEncoding("UTF-8")
		source.setDefaultLocale(Locale.ENGLISH)
		source.setFallbackToSystemLocale(false)
		return source
	}

	@Bean
	fun validator(messageSource: MessageSource): LocalValidatorFactoryBean {
		val bean = LocalValidatorFactoryBean()
		bean.setValidationMessageSource(messageSource)
		return bean
	}
}
