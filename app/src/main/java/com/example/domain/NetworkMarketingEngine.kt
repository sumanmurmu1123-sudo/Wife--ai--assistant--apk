package com.example.domain

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.mlm.MarketType
import com.example.data.mlm.ProspectEntity
import com.example.data.mlm.ProspectStage

class NetworkMarketingEngine(context: Context) {

    private val db = AppDatabase.getDatabase(context)

    private val objectionDatabase = mapOf(
        "NO_MONEY" to """
            Objection: "I don't have money to start."
            Formulas to use: Feel-Felt-Found
            Pitch: "I completely understand how you feel, [Name]. When I first looked at this project, I was in a tight financial spot too. But then I realized: if I don't change what I'm doing today, my bank balance will be the exact same one year from now. That's why I took this as a solution rather than an excuse. If I show you a step-by-step roadmap to recover your startup amount in the first 30 days, would you be open to exploring it?"
        """.trimIndent(),

        "NO_TIME" to """
            Objection: "I have a busy job, I don't have time."
            Pitch: "That makes total sense, [Name]. In fact, most top leaders in our community started this alongside their 9-to-5 job with just 6–8 hours a week. The beauty of this model is leverage—building a second stream of passive income without disturbing your main work. If we can structure a plan that takes just 1 focused hour a day, would that be worth creating financial freedom for your family?"
        """.trimIndent(),

        "IS_THIS_CHAIN_PYRAMID" to """
            Objection: "Is this a chain system or pyramid scheme?"
            Pitch: "I'm glad you asked, [Name]. Illegal pyramid schemes have no real product and only circulate money from recruitment. What we do is 100% legal Direct Selling and Ethical Product Distribution backed by government direct selling guidelines. You earn purely on consumer product volume, exactly like a franchise model (like Amul or McDonald's). If you don't like the product, there is no business. Fair enough?"
        """.trimIndent()
    )

    fun getInvitationScript(marketType: MarketType, prospectName: String): String {
        return when (marketType) {
            MarketType.HOT -> """
                🔥 Hot Market Script for $prospectName:
                "Hey $prospectName, I hope you're doing great! I recently partnered with an expanding e-commerce business project. I'm building my core team of people I truly trust, and your name was the first on my list. Let's catch up over coffee or a 15-min Zoom call this Tuesday at 7 PM to show you what we're building. Would Tuesday or Wednesday work better for you?"
            """.trimIndent()

            MarketType.WARM -> """
                🤝 Warm Market Script for $prospectName:
                "Hi $prospectName, I know you are already doing great in your field. A few business partners and I are expanding a consumer distribution network in our region, and we are looking for ambitious professionals with strong communication skills. I'm not sure if it's a fit for you, but are you open to exploring a side-project that doesn't conflict with your current job?"
            """.trimIndent()

            MarketType.COLD -> """
                ❄️ Cold Social Media Outreach for $prospectName:
                "Hi $prospectName, I came across your profile and really appreciated your work ethic and mindset. I lead an entrepreneurship community helping people monetize their network online. If I sent you a 3-minute overview video on how we do it, would you take a look?"
            """.trimIndent()
        }
    }

    fun getObjectionHandlingFormula(objectionKey: String): String {
        return objectionDatabase[objectionKey] 
            ?: "Listen with empathy, agree with their perspective ('Feel, Felt, Found'), isolate the core doubt, and ask for permission to show a solution."
    }

    fun calculateCommissionEstimate(totalTeamBv: Double, commissionRatePercent: Double): String {
        val payout = totalTeamBv * (commissionRatePercent / 100.0)
        return "Based on total Team Business Volume (BV) of $totalTeamBv at $commissionRatePercent% matching tier, your estimated bonus payout is ₹${"%.2f".format(payout)}."
    }
}
