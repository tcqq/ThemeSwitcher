package com.example.themeswitcher.model.pref

import com.example.themeswitcher.manager.DEFAULT_THEME
import com.chibatching.kotpref.KotprefModel

/**
 * @author Alan Dreamer
 * @since 2018/06/11 Created
 */
object ThemeOverlayPref : KotprefModel() {
    override val kotprefName = "theme_overlay"
    var startHourNightMode by intPref(22, "start_hour_night_mode")
    var startMinuteNightMode by intPref(0, "start_minute_night_mode")
    var endHourNightMode by intPref(6, "end_hour_night_mode")
    var endMinuteNightMode by intPref(0, "end_minute_night_mode")
    var darkTheme by booleanPref(false, "dark_theme")
    var theme by intPref(DEFAULT_THEME, "theme")
}