package com.hoaison.landmarkremark.common

import android.content.Context
import androidx.core.content.edit
import com.hoaison.landmarkremark.model.User
import com.hoaison.landmarkremark.util.fromJson
import com.hoaison.landmarkremark.util.toJson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(@ApplicationContext context: Context) {
    companion object {
        private const val PREF_FILE = "APP_PREF"

        private const val KEY_USER = "KEY_USER"
    }

    private val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun clear() = prefs.edit() { clear() }

    var user: User?
        get() {
            val data = prefs.getString(KEY_USER, "")
            return if (data.isNullOrEmpty()) null else data.fromJson()
        }
        set(value) = prefs.edit() {putString(KEY_USER, value.toJson()) }
}