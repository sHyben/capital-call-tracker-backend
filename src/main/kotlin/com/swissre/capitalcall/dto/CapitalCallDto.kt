package com.swissre.capitalcall.dto

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import java.math.BigDecimal
import java.time.LocalDate

data class CapitalCallDto(
	val id: Long,
	val investorId: Long,
	val investorName: String,
	val amount: BigDecimal,
	val dueDate: LocalDate,
	val status: CapitalCallStatus,
	val noticeDocumentUrl: String?
)

fun CapitalCall.toDto() = CapitalCallDto(
	id = id,
	investorId = investor.id,
	investorName = investor.name,
	amount = amount,
	dueDate = dueDate,
	status = status,
	noticeDocumentUrl = noticeDocumentUrl
)
