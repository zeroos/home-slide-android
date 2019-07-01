package fr.outadoc.quickhass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import fr.outadoc.quickhass.preferences.PreferenceRepository
import fr.outadoc.quickhass.preferences.PreferenceRepositoryImpl

abstract class DayNightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyPreferredTheme()
    }

    private fun applyPreferredTheme() {
        val prefs: PreferenceRepository = PreferenceRepositoryImpl(applicationContext)

        AppCompatDelegate.setDefaultNightMode(
            when (prefs.theme) {
                "day" -> AppCompatDelegate.MODE_NIGHT_NO
                "night" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

}