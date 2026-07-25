package com.tiendamuna.lintrules


import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class CustomIssueRegistry : IssueRegistry() {
    override val issues = listOf(
        LiveDataUsageDetector.ISSUE,
        // ... otras reglas que tengas
    )

    override val api: Int
        get() = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Mi Proyecto",
        feedbackUrl = "https://github.com/tu-repo/issues"
    )
}