package com.example.themeswitcher.model

/**
 * @author Alan Perry
 * @since 2018/05/27 Created
 */
object SettingsModel {
    /**
     * Theme settings
     */
    var theme: MutableSet<String> = mutableSetOf()
    var themeCurrent: MutableSet<String> = mutableSetOf()
}