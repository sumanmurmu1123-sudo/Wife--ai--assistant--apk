package com.example.v2.core.sync

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

interface CloudAuthRepository {
    val currentUserEmail: String?
    fun isUserLoggedIn(): Boolean
    suspend fun signInWithGoogle(context: Context): Result<String>
    suspend fun signOut()
}

class FirebaseAuthRepository(private val authOverride: FirebaseAuth? = null) : CloudAuthRepository {
    private val auth: FirebaseAuth? by lazy {
        authOverride ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // Only log if it's not just a "not initialized" issue which we expect if google-services.json is missing
            if (e.message?.contains("FirebaseApp is not initialized") != true) {
                android.util.Log.e("AuthRepo", "Failed to get FirebaseAuth: ${e.message}")
            }
            null
        }
    }

    override val currentUserEmail: String?
        get() = auth?.currentUser?.email

    override fun isUserLoggedIn(): Boolean = auth?.currentUser != null

    override suspend fun signInWithGoogle(context: Context): Result<String> {
        val authInstance = auth ?: return Result.failure(
            Exception("Cloud Sync requires Firebase. Please configure Firebase in the AI Studio Settings or provide manual keys in your Secrets.")
        )
        return try {
            val credentialManager = CredentialManager.create(context)
            
            // This requires a real webClientId in strings.xml or build.gradle.
            // For now, we use a placeholder or check if it exists.
            val webClientId = "PLACEHOLDER_WEB_CLIENT_ID" // Ideally from BuildConfig
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = GoogleAuthProvider.getCredential(result.credential.data.getString("idToken"), null)
            
            val authResult = authInstance.signInWithCredential(credential).await()
            val email = authResult.user?.email ?: "Unknown"
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth?.signOut()
    }
}
