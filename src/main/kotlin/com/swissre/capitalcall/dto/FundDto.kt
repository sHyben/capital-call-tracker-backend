package com.swissre.capitalcall.dto

import com.swissre.capitalcall.domain.Fund
import java.math.BigDecimal

data class FundDto(
	val id: Long,
	val name: String,
	val targetSize: BigDecimal
)

fun Fund.toDto() = FundDto(
	id = id,
	name = name,
	targetSize = targetSize
)
