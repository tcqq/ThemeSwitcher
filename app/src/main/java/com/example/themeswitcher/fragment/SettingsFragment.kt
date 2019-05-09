package com.example.themeswitcher.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.themeswitcher.R
import com.example.themeswitcher.activity.ThemeSettingsActivity


/**
 * @author Alan Dreamer
 * @since 11/04/2018 Created
 */
class SettingsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)

        val themePreference = findPreference<Preference>("preference_theme")!!
        themePreference.onPreferenceClickListener = this
    }


    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "preference_theme" -> startActivity(Intent(activity, ThemeSettingsActivity::class.java))
        }
        return false
    }
}
