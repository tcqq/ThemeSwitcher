package com.example.themeswitcher.activity

import android.os.Bundle
import com.example.themeswitcher.R
import com.example.themeswitcher.enums.ScheduleNightMode
import com.example.themeswitcher.event.SettingsEvent
import com.example.themeswitcher.fragment.ThemeSettingsFragment
import com.example.themeswitcher.listener.ThemeSettingsListener
import com.example.themeswitcher.manager.ThemeOverlayManager
import com.example.themeswitcher.model.ColorPickerModel
import com.example.themeswitcher.model.ColorfulThemeModel
import com.example.themeswitcher.model.NightModeModel
import com.example.themeswitcher.model.SettingsModel
import com.ncapdevi.fragnav.FragNavController
import com.takisoft.preferencex.TimePickerPreference
import kotlinx.android.synthetic.main.activity_frame.*
import org.greenrobot.eventbus.EventBus

/**
 * @author Alan Dreamer
 * @since 2018/04/12 Created
 */
class ThemeSettingsActivity : BaseActivity(),
    ThemeSettingsListener {

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
        setActionBar(toolbar)
        setActionBarTitle(R.string.theme_settings_theme)
        initFragNavController(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeOverlayManager.dispose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (SettingsModel.themeBefore != SettingsModel.theme) {
            EventBus.getDefault().post(SettingsEvent(true))
        }
    }

    private fun initFragNavController(savedInstanceState: Bundle?) {
        fragNavController.apply {
            rootFragments = listOf(ThemeSettingsFragment.newInstance())
        }
        fragNavController.initialize(FragNavController.TAB1, savedInstanceState)
    }

    override fun onScheduleUpdate(schedule: ScheduleNightMode) {
        ThemeOverlayManager.applySchedule(this, this, schedule)
    }

    override fun onNightModeUpdate(model: NightModeModel) {
        ThemeOverlayManager.applyNightMode(this, this, model)
    }

    override fun onStartTimeUpdate(timeWrapper: TimePickerPreference.TimeWrapper) {
        ThemeOverlayManager.applyStartTime(this, this, timeWrapper)
    }

    override fun onEndTimeUpdate(timeWrapper: TimePickerPreference.TimeWrapper) {
        ThemeOverlayManager.applyEndTime(this, this, timeWrapper)
    }

    override fun onPrimaryColorUpdate(model: ColorPickerModel, clearPrimaryColorHax: Boolean, colorfulMode: Boolean) {
        ThemeOverlayManager.applyPrimaryColor(this, model, clearPrimaryColorHax, colorfulMode)
    }

    override fun onSecondaryColorUpdate(model: ColorPickerModel, colorfulMode: Boolean, nightMode: Boolean) {
        ThemeOverlayManager.applySecondaryColor(this, model, colorfulMode, nightMode)
    }

    override fun onColorfulThemeUpdate(model: ColorfulThemeModel) {
        ThemeOverlayManager.applyColorfulTheme(this, model)
    }
}
