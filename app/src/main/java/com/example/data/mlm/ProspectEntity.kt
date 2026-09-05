package com.example.data.mlm

import androidx.room.*
import java.util.UUID

enum class ProspectStage {
    NEW_LEAD,       // Just invited / Connected
    PLAN_SHOWN,     // Presentation completed
    FOLLOW_UP,      // Awaiting decision
    JOINED,         // Successfully converted to Downline
    REJECTED        // Not interested for now
}

enum class MarketType {
    HOT,   // Close friends & family
    WARM,  // Acquaintances, colleagues
    COLD   // Social media leads, strangers
}

@Entity(tableName = "network_prospects")
data class ProspectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val marketType: MarketType = MarketType.WARM,
    val stage: ProspectStage = ProspectStage.NEW_LEAD,
    val notes: String = "",
    val nextFollowUpTimestamp: Long = System.currentTimeMillis() + 86400000L // Default next day
)
