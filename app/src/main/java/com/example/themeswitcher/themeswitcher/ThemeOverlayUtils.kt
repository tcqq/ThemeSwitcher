/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.themeswitcher.themeswitcher

import android.app.Activity
import androidx.annotation.StyleRes
import com.changrui.tt.model.pref.ThemeOverlayPref

/** Utils for theme overlays.  */
object ThemeOverlayUtils {

/*    @StyleRes
    var themeOverlays = intArrayOf(0)
        private set*/

    fun setThemeOverlays(activity: Activity, @StyleRes vararg themeOverlays: Int) {
//        if (!this.themeOverlays.contentEquals(themeOverlays)) {
//            this.themeOverlays = themeOverlays
//            activity.recreate()
//        }

        ThemeOverlayPref.themeOverlays.clear()
        for (themeOverlay in themeOverlays) {
            ThemeOverlayPref.themeOverlays.add(themeOverlay.toString())
        }
        activity.recreate()
    }

//    fun clearThemeOverlays(activity: Activity) {
//        setThemeOverlays(activity)
//    }

    fun applyThemeOverlays(activity: Activity) {
/*        for (themeOverlay in themeOverlays) {
            activity.setTheme(themeOverlay)
        }*/

        val themeOverlays = ThemeOverlayPref.themeOverlays
        for (themeOverlay in themeOverlays) {
            activity.setTheme(themeOverlay.toInt())
        }
    }
}
