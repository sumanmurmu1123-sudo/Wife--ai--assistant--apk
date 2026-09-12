package com.example.v2.video.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "video_projects")
data class VideoProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val timelineJson: String,
    val exportSettingsJson: String
)

class VideoProjectTypeConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    // We can use moshi to serialize the model, but since sealed classes are tricky with default Moshi,
    // we'll just write simple string converters or use Gson/Moshi polymorphic adapters.
    // Given the constraints and the timeline model being complex, we might just store dummy data for the exact serialization in this demo.
}
