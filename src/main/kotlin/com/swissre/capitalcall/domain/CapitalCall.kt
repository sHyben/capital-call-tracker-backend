package com.swissre.capitalcall.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "capital_calls")
class CapitalCall(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long = 0,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investor_id", nullable = false)
	var investor: Investor,

	var amount: BigDecimal,

	@Column(name = "due_date", nullable = false)
	var dueDate: LocalDate,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var status: CapitalCallStatus = CapitalCallStatus.DRAFT,

	@Column(name = "notice_document_url")
	var noticeDocumentUrl: String? = null
)
