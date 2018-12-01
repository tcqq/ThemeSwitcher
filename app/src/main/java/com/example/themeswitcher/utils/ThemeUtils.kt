package com.example.themeswitcher.utils

import android.content.Context
import android.util.TypedValue

/**
 * @author Alan Dreamer
 * @since 2018/07/22 Created
 */
object ThemeUtils {

    fun getThemeValue(resId: Int, context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(resId, value, true)
        return value.data
    }
}