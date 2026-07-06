package com.swissre.capitalcall.repository

import com.swissre.capitalcall.domain.CapitalCall
import org.springframework.data.jpa.repository.JpaRepository

interface CapitalCallRepository : JpaRepository<CapitalCall, Long> {
	fun findByInvestorId(investorId: Long): List<CapitalCall>
	fun findByInvestorEntraObjectId(entraObjectId: String): List<CapitalCall>
}
