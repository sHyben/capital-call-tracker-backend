package com.swissre.capitalcall.service

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.domain.Fund
import com.swissre.capitalcall.domain.Investor
import com.swissre.capitalcall.dto.CapitalCallDto
import com.swissre.capitalcall.dto.toDto
import com.swissre.capitalcall.repository.CapitalCallRepository
import com.swissre.capitalcall.web.CapitalCallNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NoticeGenerationService(
	private val capitalCallRepository: CapitalCallRepository
) {

	/**
	 * Structured concurrency showcase: the fund letterhead and the investor's banking
	 * reference are independent lookups, so they run concurrently via async {} and are
	 * both awaited before rendering — not just a delay() for its own sake.
	 */
	suspend fun generateNotice(capitalCallId: Long): CapitalCallDto = coroutineScope {
		val capitalCall = findCapitalCall(capitalCallId)
		capitalCall.status = CapitalCallStatus.NOTICE_GENERATING
		saveCapitalCall(capitalCall)

		val letterheadDeferred = async { fetchFundLetterhead(capitalCall.investor.fund) }
		val bankingReferenceDeferred = async { fetchInvestorBankingReference(capitalCall.investor) }

		val letterhead = letterheadDeferred.await()
		val bankingReference = bankingReferenceDeferred.await()

		val documentUrl = renderNotice(capitalCall, letterhead, bankingReference)

		capitalCall.status = CapitalCallStatus.ISSUED
		capitalCall.noticeDocumentUrl = documentUrl
		saveCapitalCall(capitalCall).toDto()
	}

	private suspend fun findCapitalCall(id: Long): CapitalCall = withContext(Dispatchers.IO) {
		capitalCallRepository.findById(id).orElseThrow { CapitalCallNotFoundException(id) }
	}

	private suspend fun saveCapitalCall(capitalCall: CapitalCall): CapitalCall = withContext(Dispatchers.IO) {
		capitalCallRepository.save(capitalCall)
	}

	private suspend fun fetchFundLetterhead(fund: Fund): String = withContext(Dispatchers.IO) {
		delay(150)
		"letterhead:${fund.id}:${fund.name}"
	}

	private suspend fun fetchInvestorBankingReference(investor: Investor): String = withContext(Dispatchers.IO) {
		delay(150)
		"banking-ref:${investor.id}:${investor.name.hashCode()}"
	}

	private fun renderNotice(capitalCall: CapitalCall, letterhead: String, bankingReference: String): String {
		val documentId = UUID.randomUUID()
		return "https://notices.local/documents/$documentId?letterhead=$letterhead&ref=$bankingReference&call=${capitalCall.id}"
	}
}
