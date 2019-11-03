package com.example.themeswitcher.activity

import android.os.Bundle
import com.changrui.tt.model.pref.ThemeOverlayPref
import com.example.themeswitcher.R
import com.example.themeswitcher.event.ThemeChangeSettingsEvent
import com.example.themeswitcher.fragment.ThemeSettingsFragment
import com.example.themeswitcher.model.SettingsModel
import com.ncapdevi.fragnav.FragNavController
import kotlinx.android.synthetic.main.activity_frame.*
import org.greenrobot.eventbus.EventBus

/**
 * @author Alan Perry
 * @since 2018/04/12 Created
 */
class ThemeSettingsActivity : BaseActivity() {

    private val fragNavController: FragNavController by lazy {
        FragNavController(supportFragmentManager, R.id.container)
    }

    private fun themeChanged(): Boolean =
        SettingsModel.themeCurrent.containsAll(SettingsModel.theme).not() ||
                (SettingsModel.theme.size == 0 && SettingsModel.themeCurrent.size != 0)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame)
        setActionBar(toolbar)
        setActionBarTitle(R.string.theme_settings_theme)
        initFragNavController(savedInstanceState)
        initThemeSettings()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (themeChanged()) {
            EventBus.getDefault().post(ThemeChangeSettingsEvent())
        }
    }

    private fun initFragNavController(savedInstanceState: Bundle?) {
        fragNavController.apply {
            rootFragments = listOf(ThemeSettingsFragment.newInstance())
        }
        fragNavController.initialize(FragNavController.TAB1, savedInstanceState)
    }

    private fun initThemeSettings() {
        val themeOverlays = ThemeOverlayPref.themeOverlays
        SettingsModel.themeCurrent.clear()
        SettingsModel.themeCurrent.addAll(themeOverlays)
    }
}