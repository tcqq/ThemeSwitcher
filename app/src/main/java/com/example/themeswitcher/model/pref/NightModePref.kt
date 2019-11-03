package com.example.themeswitcher.model.pref

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.example.themeswitcher.enums.NightMode

/**
 * @author Alan Perry
 * @since 2019-04-13 Created
 */
object NightModePref : KotprefModel() {
    override val kotprefName = "night_mode"
    var nightMode by enumValuePref(NightMode.MODE_NIGHT_NO)
}