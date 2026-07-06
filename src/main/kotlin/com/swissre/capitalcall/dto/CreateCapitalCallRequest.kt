package com.swissre.capitalcall.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class CreateCapitalCallRequest(
	@field:NotNull(message = "{validation.investorId.required}")
	val investorId: Long?,

	@field:NotNull(message = "{validation.amount.required}")
	@field:Positive(message = "{validation.amount.positive}")
	val amount: BigDecimal?,

	@field:NotNull(message = "{validation.dueDate.required}")
	@field:Future(message = "{validation.dueDate.future}")
	val dueDate: LocalDate?
)
