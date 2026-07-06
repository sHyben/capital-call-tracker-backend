package com.swissre.capitalcall.service

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.dto.CapitalCallDto
import com.swissre.capitalcall.dto.CreateCapitalCallRequest
import com.swissre.capitalcall.dto.toDto
import com.swissre.capitalcall.repository.CapitalCallRepository
import com.swissre.capitalcall.repository.InvestorRepository
import com.swissre.capitalcall.web.CapitalCallExceedsCommitmentException
import com.swissre.capitalcall.web.InvestorNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CapitalCallService(
	private val capitalCallRepository: CapitalCallRepository,
	private val investorRepository: InvestorRepository
) {

	fun findMine(entraObjectId: String): List<CapitalCallDto> =
		capitalCallRepository.findByInvestorEntraObjectId(entraObjectId).map { it.toDto() }

	fun findByInvestor(investorId: Long): List<CapitalCallDto> {
		if (!investorRepository.existsById(investorId)) {
			throw InvestorNotFoundException(investorId)
		}
		return capitalCallRepository.findByInvestorId(investorId).map { it.toDto() }
	}

	@Transactional
	fun create(request: CreateCapitalCallRequest): CapitalCallDto {
		val investor = investorRepository.findById(request.investorId!!)
			.orElseThrow { InvestorNotFoundException(request.investorId) }

		val amount = request.amount!!
		if (amount > investor.remainingCommitment()) {
			throw CapitalCallExceedsCommitmentException(amount, investor.remainingCommitment())
		}

		val capitalCall = CapitalCall(
			investor = investor,
			amount = amount,
			dueDate = request.dueDate!!,
			status = CapitalCallStatus.DRAFT
		)
		return capitalCallRepository.save(capitalCall).toDto()
	}
}
