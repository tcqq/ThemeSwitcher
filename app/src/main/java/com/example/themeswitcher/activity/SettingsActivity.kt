package com.example.themeswitcher.activity

import android.os.Bundle
import com.changrui.tt.model.pref.ThemeOverlayPref
import com.example.themeswitcher.R
import com.example.themeswitcher.event.ThemeChangeSettingsEvent
import com.example.themeswitcher.fragment.SettingsFragment
import com.example.themeswitcher.model.SettingsModel
import com.ncapdevi.fragnav.FragNavController
import kotlinx.android.synthetic.main.activity_frame.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


/**
 * @author Alan Dreamer
 * @since 2017/06/26 Created
 */
class SettingsActivity : BaseActivity() {

    private val fragNavController: FragNavController by lazy {
        FragNavController(supportFragmentManager, R.id.container)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame)
        EventBus.getDefault().register(this)
        setActionBar(toolbar)
        setActionBarTitle(R.string.settings_title)
        initThemeSettings()
        initFragNavController(savedInstanceState)
    }

    private fun initThemeSettings() {
        val themeOverlays = ThemeOverlayPref.themeOverlays
        SettingsModel.theme.clear()
        SettingsModel.themeCurrent.clear()
        SettingsModel.theme.addAll(themeOverlays)
        SettingsModel.themeCurrent.addAll(themeOverlays)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onThemeChangeEvent(event: ThemeChangeSettingsEvent) {
        Timber.d("onThemeChangeEvent")
        recreate()
    }

    private fun initFragNavController(savedInstanceState: Bundle?) {
        fragNavController.apply {
            rootFragments = listOf(SettingsFragment.newInstance())
        }
        fragNavController.initialize(FragNavController.TAB1, savedInstanceState)
    }
}
