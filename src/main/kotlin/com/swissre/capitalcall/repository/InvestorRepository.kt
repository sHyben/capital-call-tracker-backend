package com.swissre.capitalcall.repository

import com.swissre.capitalcall.domain.Investor
import org.springframework.data.jpa.repository.JpaRepository

interface InvestorRepository : JpaRepository<Investor, Long> {
	fun findByFundId(fundId: Long): List<Investor>
	fun findByEntraObjectId(entraObjectId: String): Investor?
}
