package com.swissre.capitalcall.controller

import com.swissre.capitalcall.dto.CapitalCallDto
import com.swissre.capitalcall.dto.CreateCapitalCallRequest
import com.swissre.capitalcall.service.CapitalCallService
import com.swissre.capitalcall.service.NoticeGenerationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
class CapitalCallController(
	private val capitalCallService: CapitalCallService,
	private val noticeGenerationService: NoticeGenerationService
) {

	// Scoped server-side by the validated JWT's oid claim — never by a client-supplied investor id.
	@GetMapping("/api/capital-calls/mine")
	@PreAuthorize("hasRole('Investor')")
	fun getMine(@AuthenticationPrincipal jwt: Jwt): List<CapitalCallDto> =
		capitalCallService.findMine(jwt.getClaimAsString("oid"))

	@GetMapping("/api/capital-calls/by-investor/{investorId}")
	@PreAuthorize("hasRole('FundManager')")
	fun getByInvestor(@PathVariable investorId: Long): List<CapitalCallDto> =
		capitalCallService.findByInvestor(investorId)

	@PostMapping("/api/capital-calls")
	@PreAuthorize("hasRole('FundManager')")
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: CreateCapitalCallRequest): CapitalCallDto =
		capitalCallService.create(request)

	@PostMapping("/api/capital-calls/{id}/generate-notice")
	@PreAuthorize("hasRole('FundManager')")
	suspend fun generateNotice(@PathVariable id: Long): CapitalCallDto =
		noticeGenerationService.generateNotice(id)
}
