package com.example.themeswitcher.listener

import com.example.themeswitcher.enums.ScheduleNightMode
import com.example.themeswitcher.model.ColorPickerModel
import com.example.themeswitcher.model.ColorfulThemeModel
import com.example.themeswitcher.model.NightModeModel
import com.takisoft.preferencex.TimePickerPreference

/**
 * @author Alan Dreamer
 * @since 30/05/2018 Created
 */
interface ThemeSettingsListener {
    fun onScheduleUpdate(schedule: ScheduleNightMode)
    fun onNightModeUpdate(model: NightModeModel)
    fun onStartTimeUpdate(timeWrapper: TimePickerPreference.TimeWrapper)
    fun onEndTimeUpdate(timeWrapper: TimePickerPreference.TimeWrapper)
    fun onPrimaryColorUpdate(model: ColorPickerModel, clearPrimaryColorHax: Boolean, colorfulMode: Boolean)
    fun onSecondaryColorUpdate(model: ColorPickerModel, colorfulMode: Boolean, nightMode: Boolean)
    fun onColorfulThemeUpdate(model: ColorfulThemeModel)
}