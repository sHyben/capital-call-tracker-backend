package com.swissre.capitalcall.config

import com.swissre.capitalcall.domain.CapitalCall
import com.swissre.capitalcall.domain.CapitalCallStatus
import com.swissre.capitalcall.domain.Fund
import com.swissre.capitalcall.domain.Investor
import com.swissre.capitalcall.repository.CapitalCallRepository
import com.swissre.capitalcall.repository.FundRepository
import com.swissre.capitalcall.repository.InvestorRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Seeds one fund and two investors so the app has data to show on first run.
 * investorA's entraObjectId is a placeholder — replace it with your own `oid` claim
 * (decode a signed-in access token at jwt.ms) after the first login, so /capital-calls/mine
 * has matching data for your account.
 */
@Component
class DemoDataSeeder(
	private val fundRepository: FundRepository,
	private val investorRepository: InvestorRepository,
	private val capitalCallRepository: CapitalCallRepository
) : CommandLineRunner {

	override fun run(vararg args: String?) {
		if (fundRepository.count() > 0) return

		val fund = fundRepository.save(
			Fund(name = "Alternative Capital Partners Fund III", targetSize = BigDecimal("500000000"))
		)

		val investorA = investorRepository.save(
			Investor(
				name = "Meridian Pension Trust",
				totalCommitment = BigDecimal("25000000"),
				calledToDate = BigDecimal("10000000"),
				entraObjectId = "REPLACE-WITH-YOUR-OID",
				fund = fund
			)
		)

		val investorB = investorRepository.save(
			Investor(
				name = "Northbridge Family Office",
				totalCommitment = BigDecimal("15000000"),
				calledToDate = BigDecimal("5000000"),
				entraObjectId = "00000000-0000-0000-0000-000000000000",
				fund = fund
			)
		)

		capitalCallRepository.save(
			CapitalCall(
				investor = investorA,
				amount = BigDecimal("2000000"),
				dueDate = LocalDate.now().plusMonths(1),
				status = CapitalCallStatus.DRAFT
			)
		)

		capitalCallRepository.save(
			CapitalCall(
				investor = investorB,
				amount = BigDecimal("1500000"),
				dueDate = LocalDate.now().plusMonths(2),
				status = CapitalCallStatus.ISSUED,
				noticeDocumentUrl = "https://example.com/notices/demo-notice.pdf"
			)
		)
	}
}
