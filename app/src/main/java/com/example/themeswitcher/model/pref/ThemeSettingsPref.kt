package com.example.themeswitcher.model.pref

import com.chibatching.kotpref.KotprefModel

/**
 * @author Alan Dreamer
 * @since 2018/05/29 Created
 */
object ThemeSettingsPref : KotprefModel() {
    override val kotprefName = "theme_settings"
    var primaryColorHax by stringPref("#673AB7", "primary_color_hax")
    var secondaryColorHax by stringPref("#E91E63", "secondary_color_hax")
    var colorPickerPrimaryColorHax by nullableStringPref(null, "color_picker_primary_color_hax")
}