package com.swissre.capitalcall.repository

import com.swissre.capitalcall.domain.Fund
import org.springframework.data.jpa.repository.JpaRepository

interface FundRepository : JpaRepository<Fund, Long>
