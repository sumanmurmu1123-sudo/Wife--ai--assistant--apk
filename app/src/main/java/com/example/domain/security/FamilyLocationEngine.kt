package com.example.domain.security

import android.content.Context

class FamilyLocationEngine(private val context: Context) {
    fun locateFamilyMembers(): String {
        return "Simulated Family Locations: Sunit (At Office), Mom (At Home)."
    }
}
