package com.swissre.capitalcall.dto

import com.swissre.capitalcall.domain.Investor
import java.math.BigDecimal

data class InvestorDto(
	val id: Long,
	val name: String,
	val totalCommitment: BigDecimal,
	val calledToDate: BigDecimal,
	val remainingCommitment: BigDecimal,
	val fundId: Long
)

fun Investor.toDto() = InvestorDto(
	id = id,
	name = name,
	totalCommitment = totalCommitment,
	calledToDate = calledToDate,
	remainingCommitment = remainingCommitment(),
	fundId = fund.id
)
