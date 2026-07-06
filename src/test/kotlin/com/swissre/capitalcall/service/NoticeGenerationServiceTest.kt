package com.swissre.capitalcall.service

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.domain.Fund
import com.swissre.capitalcall.domain.Investor
import com.swissre.capitalcall.repository.CapitalCallRepository
import com.swissre.capitalcall.web.CapitalCallNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class NoticeGenerationServiceTest {

	private val capitalCallRepository = mockk<CapitalCallRepository>()
	private val service = NoticeGenerationService(capitalCallRepository)

	@Test
	fun `generateNotice transitions DRAFT to NOTICE_GENERATING then ISSUED with a document url`() = runTest {
		val fund = Fund(id = 1, name = "Fund I", targetSize = BigDecimal("1000000"))
		val investor = Investor(
			id = 1,
			name = "Investor A",
			totalCommitment = BigDecimal("100000"),
			calledToDate = BigDecimal("40000"),
			entraObjectId = "oid-123",
			fund = fund
		)
		val capitalCall = CapitalCall(
			id = 5,
			investor = investor,
			amount = BigDecimal("10000"),
			dueDate = LocalDate.now().plusMonths(1),
			status = CapitalCallStatus.DRAFT
		)

		every { capitalCallRepository.findById(5L) } returns Optional.of(capitalCall)
		val savedStatuses = mutableListOf<CapitalCallStatus>()
		val savedSlot = slot<CapitalCall>()
		every { capitalCallRepository.save(capture(savedSlot)) } answers {
			savedStatuses.add(savedSlot.captured.status)
			savedSlot.captured
		}

		val result = service.generateNotice(5L)

		assertEquals(CapitalCallStatus.ISSUED, result.status)
		assertNotNull(result.noticeDocumentUrl)
		assertEquals(listOf(CapitalCallStatus.NOTICE_GENERATING, CapitalCallStatus.ISSUED), savedStatuses)
		verify(exactly = 2) { capitalCallRepository.save(any()) }
	}

	@Test
	fun `generateNotice throws when capital call does not exist`() = runTest {
		every { capitalCallRepository.findById(404L) } returns Optional.empty()

		var thrown: Throwable? = null
		try {
			service.generateNotice(404L)
		} catch (ex: CapitalCallNotFoundException) {
			thrown = ex
		}

		assertNotNull(thrown)
	}
}
