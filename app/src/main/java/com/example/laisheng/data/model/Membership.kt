package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class MembershipPlan(
    val code: String,
    val label: String,
    val months: Int,
    val amount: Double,
    @SerializedName("folder_limit") val folderLimit: Int,
    @SerializedName("history_retention_days") val historyRetentionDays: Int,
    @SerializedName("max_page_size") val maxPageSize: Int,
    val benefits: List<String> = emptyList()
)

data class MembershipStatus(
    val status: String,
    val level: String,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("is_active") val isActive: Boolean = false,
    val plan: MembershipPlan? = null,
    val benefits: List<String> = emptyList(),
    @SerializedName("folder_limit") val folderLimit: Int = 10,
    @SerializedName("history_retention_days") val historyRetentionDays: Int = 30,
    @SerializedName("max_page_size") val maxPageSize: Int = 20
)

data class MembershipOrder(
    val id: String? = null,
    @SerializedName("plan_code") val planCode: String? = null,
    @SerializedName("plan_label") val planLabel: String? = null,
    val amount: Double? = null,
    val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)

data class MembershipActivateRequest(
    @SerializedName("plan_code") val planCode: String
)
