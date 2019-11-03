package com.changrui.tt.model.pref

import com.chibatching.kotpref.KotprefModel
import java.util.*

/**
 * @author Alan Perry
 * @since 2019-04-09 Created
 */
object ThemeOverlayPref: KotprefModel() {
    override val kotprefName = "theme_overlay"
    val themeOverlays by stringSetPref("theme_overlays") {
        return@stringSetPref TreeSet<String>()
    }
}