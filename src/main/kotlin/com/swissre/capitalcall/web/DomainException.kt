package com.swissre.capitalcall.web

import org.springframework.http.HttpStatus
import java.math.BigDecimal

sealed class DomainException(
	val messageKey: String,
	val status: HttpStatus,
	val args: Array<Any?> = emptyArray()
) : RuntimeException(messageKey)

class FundNotFoundException(id: Long) :
	DomainException("error.fund.notFound", HttpStatus.NOT_FOUND, arrayOf(id))

class InvestorNotFoundException(id: Long) :
	DomainException("error.investor.notFound", HttpStatus.NOT_FOUND, arrayOf(id))

class CapitalCallNotFoundException(id: Long) :
	DomainException("error.capitalCall.notFound", HttpStatus.NOT_FOUND, arrayOf(id))

class CapitalCallExceedsCommitmentException(amount: BigDecimal, remaining: BigDecimal) :
	DomainException("error.capitalCall.amountExceedsCommitment", HttpStatus.UNPROCESSABLE_ENTITY, arrayOf(amount, remaining))
