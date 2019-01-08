package com.example.themeswitcher.model.pref

import com.example.themeswitcher.manager.DEFAULT_PRIMARY_COLOR_HAX
import com.example.themeswitcher.manager.DEFAULT_SECONDARY_COLOR_HAX
import com.chibatching.kotpref.KotprefModel

/**
 * @author Alan Dreamer
 * @since 2018/05/29 Created
 */
object ThemeSettingsPref : KotprefModel() {
    override val kotprefName = "theme_settings"
    var primaryColorHax by stringPref(DEFAULT_PRIMARY_COLOR_HAX, "primary_color_hax")
    var secondaryColorHax by stringPref(DEFAULT_SECONDARY_COLOR_HAX, "secondary_color_hax")
}