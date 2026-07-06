package com.swissre.capitalcall.service

import com.swissre.capitalcall.dto.FundDto
import com.swissre.capitalcall.dto.InvestorDto
import com.swissre.capitalcall.dto.toDto
import com.swissre.capitalcall.repository.FundRepository
import com.swissre.capitalcall.repository.InvestorRepository
import com.swissre.capitalcall.web.FundNotFoundException
import org.springframework.stereotype.Service

@Service
class FundService(
	private val fundRepository: FundRepository,
	private val investorRepository: InvestorRepository
) {

	fun getAllFunds(): List<FundDto> = fundRepository.findAll().map { it.toDto() }

	fun getInvestorsForFund(fundId: Long): List<InvestorDto> {
		if (!fundRepository.existsById(fundId)) {
			throw FundNotFoundException(fundId)
		}
		return investorRepository.findByFundId(fundId).map { it.toDto() }
	}
}
