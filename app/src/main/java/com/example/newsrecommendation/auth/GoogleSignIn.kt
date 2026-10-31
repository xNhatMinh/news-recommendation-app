package com.example.newsrecommendation.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.newsrecommendation.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

suspend fun googleIdToken(activity: Activity): String {
    val option = GetSignInWithGoogleOption.Builder(
        activity.getString(R.string.default_web_client_id)
    ).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    val result = CredentialManager.create(activity).getCredential(activity, request)
    val credential = result.credential
    check(credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)

    return GoogleIdTokenCredential.createFrom(credential.data).idToken
}
