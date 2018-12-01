package com.example.themeswitcher.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.example.themeswitcher.R
import com.example.themeswitcher.enums.ScheduleNightMode
import com.example.themeswitcher.listener.ThemeSettingsListener
import com.example.themeswitcher.manager.ThemeOverlayManager
import com.example.themeswitcher.model.ColorPickerModel
import com.example.themeswitcher.model.ColorfulThemeModel
import com.example.themeswitcher.model.NightModeModel
import com.example.themeswitcher.model.pref.ThemeSettingsPref
import com.takisoft.preferencex.ColorPickerPreference
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.takisoft.preferencex.TimePickerPreference
import java.util.*

/**
 * Theme settings.
 *
 * @author Alan Dreamer
 * @since 2018/04/19 Created
 */
class ThemeSettingsFragment : PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener {

    private lateinit var callback: ThemeSettingsListener

    private lateinit var primaryColorColorPickerPreference: ColorPickerPreference
    private lateinit var secondaryColorColorPickerPreference: ColorPickerPreference
    private lateinit var colorfulThemeSwitchPreference: SwitchPreference
    private lateinit var nightModePreferenceCategory: PreferenceCategory
    private lateinit var nightModeSwitchPreference: SwitchPreference
    private lateinit var nightModeStartTimeTimePickerPreference: TimePickerPreference
    private lateinit var nightModeEndTimeTimePickerPreference: TimePickerPreference
    private lateinit var nightModeScheduleDropDownPreference: DropDownPreference

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = activity as ThemeSettingsListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement ThemeSettingsListener")
        }
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_theme_settings, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initPreference()
    }

    private fun initPreference() {
        primaryColorColorPickerPreference =
                findPreference("color_picker_preference_primary_color") as ColorPickerPreference
        secondaryColorColorPickerPreference =
                findPreference("color_picker_preference_secondary_color") as ColorPickerPreference
        colorfulThemeSwitchPreference = findPreference("switch_preference_colorful_theme") as SwitchPreference
        nightModePreferenceCategory = findPreference("preference_category_night_mode") as PreferenceCategory
        nightModeSwitchPreference = findPreference("switch_preference_night_mode") as SwitchPreference
        nightModeStartTimeTimePickerPreference =
                findPreference("time_picker_preference_night_mode_start_time") as TimePickerPreference
        nightModeEndTimeTimePickerPreference =
                findPreference("time_picker_preference_night_mode_end_time") as TimePickerPreference
        nightModeScheduleDropDownPreference =
                findPreference("drop_down_preference_night_mode_schedule") as DropDownPreference

        primaryColorColorPickerPreference.onPreferenceChangeListener = this
        secondaryColorColorPickerPreference.onPreferenceChangeListener = this
        colorfulThemeSwitchPreference.onPreferenceChangeListener = this
        nightModeSwitchPreference.onPreferenceChangeListener = this
        nightModeStartTimeTimePickerPreference.onPreferenceChangeListener = this
        nightModeEndTimeTimePickerPreference.onPreferenceChangeListener = this
        nightModeScheduleDropDownPreference.onPreferenceChangeListener = this

        initColorfulTheme()

        val defaultColors = resources.getIntArray(R.array.palette_colors_array)
        primaryColorColorPickerPreference.colors = defaultColors
        secondaryColorColorPickerPreference.colors = defaultColors
    }

    private fun initColorfulTheme() {
        initSchedule(nightModeScheduleDropDownPreference.value)
        if (colorfulThemeSwitchPreference.isChecked) {
            if (ThemeOverlayManager.isDarkTheme()) {
                nightModeSwitchPreference.isChecked = true
                primaryColorColorPickerPreference.isEnabled = false
                primaryColorColorPickerPreference.color = Color.parseColor("#000000")
            } else {
                nightModeSwitchPreference.isChecked = false
                primaryColorColorPickerPreference.isEnabled = true
                ThemeSettingsPref.colorPickerPrimaryColorHax?.let {
                    primaryColorColorPickerPreference.color = Color.parseColor(it)
                }
            }
        } else {
            primaryColorColorPickerPreference.isEnabled = false
            if (ThemeOverlayManager.isDarkTheme()) {
                nightModeSwitchPreference.isChecked = true
                primaryColorColorPickerPreference.color = Color.parseColor("#000000")
            } else {
                nightModeSwitchPreference.isChecked = false
                primaryColorColorPickerPreference.color = Color.parseColor("#FFFFFF")
            }
        }
    }

    private fun initSchedule(scheduleValue: String) {
        when (scheduleValue) {
            "1" -> {
                nightModeStartTimeTimePickerPreference.isVisible = false
                nightModeEndTimeTimePickerPreference.isVisible = false
                if (ThemeOverlayManager.isDarkTheme()) {
                    nightModeSwitchPreference.summary = getString(R.string.theme_settings_on) + " / " +
                            getString(R.string.theme_settings_will_never_turn_off_automatically)
                } else {
                    nightModeSwitchPreference.summary = getString(R.string.theme_settings_off) + " / " +
                            getString(R.string.theme_settings_will_never_turn_on_automatically)
                }
            }
            "2" -> {
                nightModeStartTimeTimePickerPreference.isVisible = true
                nightModeEndTimeTimePickerPreference.isVisible = true
                initTimePicker()
            }
        }
    }

    private fun initTimePicker() {
        Handler(Looper.getMainLooper()).post {
            if (ThemeOverlayManager.isDarkTheme()) {
                nightModeSwitchPreference.summary = getString(R.string.theme_settings_on) + " / " +
                        String.format(getString(R.string.night_mode_switch_off_prompt),
                                nightModeEndTimeTimePickerPreference.summary)
            } else {
                nightModeSwitchPreference.summary = getString(R.string.theme_settings_off) + " / " +
                        String.format(getString(R.string.night_mode_switch_on_prompt),
                                nightModeStartTimeTimePickerPreference.summary)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val colorPickerModel = ColorPickerModel

        val primaryColorHax = String.format("#%06X", 0xFFFFFF and primaryColorColorPickerPreference.color)
        val secondaryColorHax = String.format("#%06X", 0xFFFFFF and secondaryColorColorPickerPreference.color)
        val currentColorHax = String.format("#%06X", 0xFFFFFF and newValue.hashCode())

        colorPickerModel.primaryColorHax = primaryColorHax
        colorPickerModel.secondaryColorHax = secondaryColorHax
        colorPickerModel.currentColorHax = currentColorHax
        when (preference.key) {
            "time_picker_preference_night_mode_start_time" -> {
                val timeWrapper = newValue as TimePickerPreference.TimeWrapper
                val currentTime = timeWrapper.hour.toString() + ":" + timeWrapper.minute

                val calendar = Calendar.getInstance()
                calendar.time = nightModeStartTimeTimePickerPreference.time
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val time = hour.toString() + ":" + minute

                if (time != currentTime) {
                    initTimePicker()
                    callback.onStartTimeUpdate(timeWrapper)
                }
            }
            "time_picker_preference_night_mode_end_time" -> {
                val timeWrapper = newValue as TimePickerPreference.TimeWrapper
                val currentTime = timeWrapper.hour.toString() + ":" + timeWrapper.minute

                val calendar = Calendar.getInstance()
                calendar.time = nightModeEndTimeTimePickerPreference.time
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val time = hour.toString() + ":" + minute

                if (time != currentTime) {
                    initTimePicker()
                    callback.onEndTimeUpdate(timeWrapper)
                }
            }
            "drop_down_preference_night_mode_schedule" -> {
                initSchedule(newValue.toString())
                when (newValue.toString()) {
                    "1" -> callback.onScheduleUpdate(ScheduleNightMode.NONE)
                    "2" -> callback.onScheduleUpdate(ScheduleNightMode.CUSTOM_TIME)
                }
            }
            "switch_preference_night_mode" -> {
                when (nightModeScheduleDropDownPreference.value) {
                    "1" -> callback.onNightModeUpdate(
                            NightModeModel(
                                    newValue.toString().toBoolean(),
                                    colorfulThemeSwitchPreference.isChecked,
                                    ScheduleNightMode.NONE
                            )
                    )
                    "2" -> callback.onNightModeUpdate(
                            NightModeModel(
                                    newValue.toString().toBoolean(),
                                    colorfulThemeSwitchPreference.isChecked,
                                    ScheduleNightMode.CUSTOM_TIME
                            )
                    )
                }
            }
            "color_picker_preference_primary_color" -> callback.onPrimaryColorUpdate(
                    colorPickerModel,
                    true,
                    colorfulThemeSwitchPreference.isChecked
            )
            "color_picker_preference_secondary_color" -> callback.onSecondaryColorUpdate(
                    colorPickerModel,
                    colorfulThemeSwitchPreference.isChecked,
                    nightModeSwitchPreference.isChecked
            )
            "switch_preference_colorful_theme" -> callback.onColorfulThemeUpdate(
                    ColorfulThemeModel(
                            newValue.toString().toBoolean(),
                            nightModeSwitchPreference.isChecked
                    )
            )
        }
        return true
    }

    companion object {
        fun newInstance(): ThemeSettingsFragment {
            return ThemeSettingsFragment()
        }
    }
}
