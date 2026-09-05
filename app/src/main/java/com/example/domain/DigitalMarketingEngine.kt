package com.example.domain

enum class AdCopyFramework(val label: String) {
    AIDA("Attention, Interest, Desire, Action"),
    PAS("Problem, Agitate, Solution"),
    BAB("Before, After, Bridge")
}

enum class MarketingChannel(val displayName: String) {
    META_ADS("Meta Ads (FB/Insta)"),
    GOOGLE_SEARCH("Google Search Ads"),
    YOUTUBE_REELS("YouTube Shorts / Reels"),
    EMAIL_CAMPAIGN("Email Marketing")
}

data class RoasMetrics(
    val adSpend: Double,
    val revenueGenerated: Double,
    val roas: Double,
    val profit: Double,
    val isProfitable: Boolean
)

class DigitalMarketingEngine {

    /**
     * 1. Generate Structured Ad Copies & Hooks
     */
    fun generateAdCopy(
        productName: String,
        targetAudience: String,
        channel: MarketingChannel,
        framework: AdCopyFramework
    ): String {
        return when (framework) {
            AdCopyFramework.PAS -> """
                🎯 [PAS Framework - ${channel.displayName}] for '$productName':
                
                🛑 Problem: Struggling to get consistent, qualified results for your business without burning out?
                🔥 Agitate: Spending thousands on unoptimized campaigns and getting zero return is frustrating. Every day you wait, your competitors are capturing your market.
                💡 Solution: Discover $productName—specifically engineered for $targetAudience. Get predictable growth, automated conversions, and full peace of mind.
                👉 CTA: Tap the link below to claim your exclusive trial today!
            """.trimIndent()

            AdCopyFramework.AIDA -> """
                🎯 [AIDA Framework - ${channel.displayName}] for '$productName':
                
                🪝 Attention: Stop wasting time on outdated marketing strategies!
                ✨ Interest: $productName helps $targetAudience scale faster with automated high-converting funnels.
                ❤️ Desire: Imagine generating qualified leads on autopilot while you focus purely on closing deals.
                🚀 Action: Click 'Learn More' now to see how we deliver results in 14 days or less.
            """.trimIndent()

            AdCopyFramework.BAB -> """
                🎯 [BAB Framework - ${channel.displayName}] for '$productName':
                
                ⏳ Before: High CAC, unpredictable revenue, and confusing analytics.
                🌟 After: Consistent pipeline, 4x+ ROAS, and scalable growth effortlessly.
                🌉 Bridge: $productName is the exact bridge that takes $targetAudience from chaotic marketing to automated profitability.
                👉 CTA: Start your scaling journey today—link in bio/description!
            """.trimIndent()
        }
    }

    /**
     * 2. Calculate ROAS, CPA & Profitability
     */
    fun calculateRoas(adSpend: Double, revenue: Double, totalConversions: Int): String {
        if (adSpend <= 0.0) return "Ad spend must be greater than 0."
        val roas = revenue / adSpend
        val profit = revenue - adSpend
        val cpa = if (totalConversions > 0) adSpend / totalConversions else 0.0

        return """
            📊 Campaign Performance Breakdown:
            • Total Ad Spend: ₹${"%.2f".format(adSpend)}
            • Revenue Generated: ₹${"%.2f".format(revenue)}
            • Net Profit/Loss: ₹${"%.2f".format(profit)} (${if (profit >= 0) "✅ Profitable" else "❌ Loss"})
            • ROAS (Return on Ad Spend): ${"%.2f".format(roas)}x (${"%.1f".format(roas * 100)}%)
            • Cost Per Acquisition (CPA): ₹${"%.2f".format(cpa)} per sale/lead
        """.trimIndent()
    }

    /**
     * 3. Suggest High-CTR Hooks for Video Reels/Shorts
     */
    fun getViralReelHooks(nicheTopic: String): List<String> {
        return listOf(
            "“Stop making this ONE mistake if you want to scale $nicheTopic in 2026...”",
            "“Here is the exact framework I used to get 10x results in $nicheTopic (steal this)...”",
            "“99% of people do $nicheTopic wrong. Here is how the top 1% actually do it...”",
            "“If I lost all my followers today, here is how I’d restart $nicheTopic from scratch...”"
        )
    }
}
