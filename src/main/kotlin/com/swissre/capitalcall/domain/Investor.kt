package com.swissre.capitalcall.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "investors")
class Investor(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long = 0,

	var name: String,

	var totalCommitment: BigDecimal,

	var calledToDate: BigDecimal,

	@Column(name = "entra_object_id", nullable = false, unique = true)
	var entraObjectId: String,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fund_id", nullable = false)
	var fund: Fund
) {
	fun remainingCommitment(): BigDecimal = totalCommitment - calledToDate
}
