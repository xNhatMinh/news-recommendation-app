package com.example.newsrecommendation.storage

import android.content.Context
import java.io.File

class TokenManager(context: Context){
    private val tokenFile = File(
        context.applicationContext.noBackupFilesDir,
        "access_token"
    )

    fun saveToken(token: String){
        tokenFile.writeText(token)
    }

    fun getToken(): String? {
        if(tokenFile.exists()) {
            return tokenFile.readText()
        }
        return null
    }

    fun clearToken(){
        tokenFile.delete()
    }
}