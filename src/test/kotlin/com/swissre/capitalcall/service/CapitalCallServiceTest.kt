package com.swissre.capitalcall.service

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.domain.Fund
import com.swissre.capitalcall.domain.Investor
import com.swissre.capitalcall.dto.CreateCapitalCallRequest
import com.swissre.capitalcall.repository.CapitalCallRepository
import com.swissre.capitalcall.repository.InvestorRepository
import com.swissre.capitalcall.web.CapitalCallExceedsCommitmentException
import com.swissre.capitalcall.web.InvestorNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class CapitalCallServiceTest {

	private val capitalCallRepository = mockk<CapitalCallRepository>()
	private val investorRepository = mockk<InvestorRepository>()
	private val service = CapitalCallService(capitalCallRepository, investorRepository)

	private lateinit var fund: Fund
	private lateinit var investor: Investor

	@BeforeEach
	fun setUp() {
		fund = Fund(id = 1, name = "Fund I", targetSize = BigDecimal("1000000"))
		investor = Investor(
			id = 1,
			name = "Investor A",
			totalCommitment = BigDecimal("100000"),
			calledToDate = BigDecimal("40000"),
			entraObjectId = "oid-123",
			fund = fund
		)
	}

	@Test
	fun `findMine scopes by entraObjectId`() {
		val call = CapitalCall(id = 1, investor = investor, amount = BigDecimal("1000"), dueDate = LocalDate.now().plusDays(1))
		every { capitalCallRepository.findByInvestorEntraObjectId("oid-123") } returns listOf(call)

		val result = service.findMine("oid-123")

		assertEquals(1, result.size)
		assertEquals(investor.id, result[0].investorId)
		verify { capitalCallRepository.findByInvestorEntraObjectId("oid-123") }
	}

	@Test
	fun `findByInvestor throws when investor does not exist`() {
		every { investorRepository.existsById(99L) } returns false

		assertThrows(InvestorNotFoundException::class.java) {
			service.findByInvestor(99L)
		}
	}

	@Test
	fun `create succeeds when amount is within remaining commitment`() {
		val request = CreateCapitalCallRequest(investorId = 1, amount = BigDecimal("10000"), dueDate = LocalDate.now().plusMonths(1))
		every { investorRepository.findById(1L) } returns Optional.of(investor)

		val savedSlot = slot<CapitalCall>()
		every { capitalCallRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

		val result = service.create(request)

		assertEquals(BigDecimal("10000"), result.amount)
		assertEquals(CapitalCallStatus.DRAFT, result.status)
		assertEquals(investor.id, result.investorId)
	}

	@Test
	fun `create rejects amount exceeding remaining commitment`() {
		// remainingCommitment = 100000 - 40000 = 60000
		val request = CreateCapitalCallRequest(investorId = 1, amount = BigDecimal("70000"), dueDate = LocalDate.now().plusMonths(1))
		every { investorRepository.findById(1L) } returns Optional.of(investor)

		assertThrows(CapitalCallExceedsCommitmentException::class.java) {
			service.create(request)
		}
	}
}
