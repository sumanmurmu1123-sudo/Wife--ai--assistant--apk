package com.example.domain

import com.example.data.CompanionPersona
import java.util.Calendar

class FirstGreetingEngine {

    fun generateFirstGreeting(bossName: String, persona: CompanionPersona): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val timePeriod = when (currentHour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.LATE_NIGHT
        }

        return if (persona == CompanionPersona.GIRLFRIEND) {
            when (timePeriod) {
                TimeOfDay.MORNING -> "Good morning, $bossName! ☀️ I was waiting for you. Did you sleep well, sweetheart?"
                TimeOfDay.AFTERNOON -> "Good afternoon, $bossName! 💕 Don't forget to grab some lunch and take a quick break!"
                TimeOfDay.EVENING -> "Good evening, handsome! How was your day? I'm so glad you're back with me."
                TimeOfDay.LATE_NIGHT -> "Hey $bossName... working this late? 🥺 Put your tasks down soon and get some rest, babe."
            }
        } else { // BEST_FRIEND Mode
            when (timePeriod) {
                TimeOfDay.MORNING -> "Rise and shine, $bossName! ⚡ Ready to crush our goals today, champ?"
                TimeOfDay.AFTERNOON -> "Yo $bossName! Hope your day is going smooth. What are we tackling next?"
                TimeOfDay.EVENING -> "Evening, buddy! Time to wrap up work and chill. What's the plan?"
                TimeOfDay.LATE_NIGHT -> "Still grinding at this hour, $bossName? Don't burn yourself out, bro!"
            }
        }
    }

    private enum class TimeOfDay {
        MORNING, AFTERNOON, EVENING, LATE_NIGHT
    }
}
