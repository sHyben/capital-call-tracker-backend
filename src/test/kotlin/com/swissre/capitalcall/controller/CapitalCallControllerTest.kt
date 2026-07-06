package com.swissre.capitalcall.controller

import com.ninjasquad.springmockk.MockkBean
import com.swissre.capitalcall.config.SecurityConfig
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.dto.CapitalCallDto
import com.swissre.capitalcall.service.CapitalCallService
import com.swissre.capitalcall.service.NoticeGenerationService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(CapitalCallController::class)
@Import(SecurityConfig::class)
class CapitalCallControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockkBean
	private lateinit var capitalCallService: CapitalCallService

	@MockkBean
	private lateinit var noticeGenerationService: NoticeGenerationService

	@Test
	fun `investor role can read mine, scoped by oid from the token`() {
		every { capitalCallService.findMine("oid-abc") } returns listOf(
			CapitalCallDto(1, 1, "Investor A", BigDecimal("1000"), LocalDate.now().plusDays(1), CapitalCallStatus.DRAFT, null)
		)

		mockMvc.perform(
			get("/api/capital-calls/mine")
				.with(
					jwt()
						.jwt { it.claim("oid", "oid-abc").claim("roles", listOf("Investor")) }
						.authorities(SimpleGrantedAuthority("ROLE_Investor"))
				)
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[0].investorId").value(1))
	}

	@Test
	fun `investor role is forbidden from fund manager only endpoint`() {
		mockMvc.perform(
			get("/api/capital-calls/by-investor/1")
				.with(jwt().authorities(SimpleGrantedAuthority("ROLE_Investor")))
		)
			.andExpect(status().isForbidden)
	}

	@Test
	fun `fund manager can list calls by investor`() {
		every { capitalCallService.findByInvestor(1L) } returns listOf(
			CapitalCallDto(1, 1, "Investor A", BigDecimal("1000"), LocalDate.now().plusDays(1), CapitalCallStatus.DRAFT, null)
		)

		mockMvc.perform(
			get("/api/capital-calls/by-investor/1")
				.with(jwt().authorities(SimpleGrantedAuthority("ROLE_FundManager")))
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[0].id").value(1))
	}
}
