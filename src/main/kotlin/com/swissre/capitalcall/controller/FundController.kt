package com.swissre.capitalcall.controller

import com.swissre.capitalcall.dto.FundDto
import com.swissre.capitalcall.dto.InvestorDto
import com.swissre.capitalcall.service.FundService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FundController(private val fundService: FundService) {

	@GetMapping("/api/funds")
	@PreAuthorize("hasAnyRole('FundManager', 'Investor')")
	fun getFunds(): List<FundDto> = fundService.getAllFunds()

	@GetMapping("/api/funds/{id}/investors")
	@PreAuthorize("hasAnyRole('FundManager', 'Investor')")
	fun getInvestors(@PathVariable id: Long): List<InvestorDto> = fundService.getInvestorsForFund(id)
}
