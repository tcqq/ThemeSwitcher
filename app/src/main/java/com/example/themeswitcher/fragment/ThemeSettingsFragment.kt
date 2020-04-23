package com.example.themeswitcher.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.themeswitcher.themeswitcher.ThemeOverlayUtils
import com.example.themeswitcher.themeswitcher.ThemeSwitcherResourceProvider
import com.example.themeswitcher.R
import com.example.themeswitcher.enums.ColorsTheme
import com.example.themeswitcher.enums.NightMode
import com.example.themeswitcher.enums.ShapeSizeTheme
import com.example.themeswitcher.enums.ShapesTheme
import com.example.themeswitcher.model.pref.NightModePref
import com.example.themeswitcher.widget.ColorPickerPreference
import com.takisoft.colorpicker.ColorPickerDialog
import com.takisoft.colorpicker.OnColorSelectedListener
import timber.log.Timber


/**
 * @author Perry Lance
 * @since 2019-04-08 Created
 */
class ThemeSettingsFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener,
    OnColorSelectedListener {

    private lateinit var secondaryColorColorPickerPreference: ColorPickerPreference
    private lateinit var nightModePreference: SwitchPreference
    private lateinit var componentShapePreference: DropDownPreference
    private lateinit var angularSizePreference: DropDownPreference

    private lateinit var resourceProvider: ThemeSwitcherResourceProvider

    private fun colorHax(@ColorInt color: Int) = String.format("#%06X", 0xFFFFFF and color)

    companion object {
        fun newInstance(): ThemeSettingsFragment {
            return ThemeSettingsFragment()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_theme_settings, rootKey)
        resourceProvider = ThemeSwitcherResourceProvider()

        secondaryColorColorPickerPreference = findPreference("color_picker_preference_secondary_color")!!
        nightModePreference = findPreference("night_mode")!!
        componentShapePreference = findPreference("preference_component_shape")!!
        angularSizePreference = findPreference("preference_angular_size")!!

        secondaryColorColorPickerPreference.onPreferenceClickListener = this
        nightModePreference.onPreferenceChangeListener = this
        componentShapePreference.onPreferenceChangeListener = this
        angularSizePreference.onPreferenceChangeListener = this
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "color_picker_preference_secondary_color" -> {
                val colors = resources.getIntArray(R.array.palette_colors_array)
                val params = ColorPickerDialog.Params.Builder(context)
                    .setSelectedColor(secondaryColorColorPickerPreference.getColor())
                    .setColors(colors)
                    .setSize(colors.size)
                    .setColumns(5)
                    .build()
                val dialog = ColorPickerDialog(activity!!, this, params)
                dialog.setTitle(getString(R.string.select_a_color))
                dialog.show()
            }
        }
        return false
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            "night_mode" -> {
                NightModePref.nightMode =
                    if (newValue.toString().toBoolean()) NightMode.MODE_NIGHT_YES else NightMode.MODE_NIGHT_NO
                secondaryColorColorPickerPreference.setColor(
                    Color.parseColor(
                        resourceProvider.colorInverse(
                            colorHax(
                                secondaryColorColorPickerPreference.getColor()
                            )
                        )
                    )
                )
                ThemeOverlayUtils.setThemeOverlays(
                    activity!!,
                    themeOverlay(),
                    shapeThemeOverlay(componentShapePreference.value),
                    shapeSizeThemeOverlay(angularSizePreference.value)
                )
            }
            "preference_component_shape" -> {
                Timber.d("onPreferenceChange: $newValue")
                ThemeOverlayUtils.setThemeOverlays(
                    activity!!,
                    themeOverlay(),
                    shapeThemeOverlay(newValue.toString()),
                    shapeSizeThemeOverlay(angularSizePreference.value)
                )
            }
            "preference_angular_size" -> {
                Timber.d("onPreferenceChange: $newValue")
                ThemeOverlayUtils.setThemeOverlays(
                    activity!!,
                    themeOverlay(),
                    shapeThemeOverlay(componentShapePreference.value),
                    shapeSizeThemeOverlay(newValue.toString())
                )
            }
        }
        return true
    }

    override fun onColorSelected(color: Int) {
        val colorHax = colorHax(color)
        val currentColorHax = colorHax(secondaryColorColorPickerPreference.getColor())
        if (colorHax != currentColorHax) {
            Timber.d("onColorSelected: $colorHax")
            secondaryColorColorPickerPreference.setColor(color)
            ThemeOverlayUtils.setThemeOverlays(
                activity!!,
                themeOverlay(),
                shapeThemeOverlay(componentShapePreference.value),
                shapeSizeThemeOverlay(angularSizePreference.value)
            )
        }
    }

    private fun nightMode(): Boolean = NightModePref.nightMode == NightMode.MODE_NIGHT_YES

    @StyleRes
    private fun themeOverlay(): Int {
        return if (secondaryColorColorPickerPreference.getColor() != 0) {
            resourceProvider.primaryThemeOverlay(
                resourceProvider.themeColor(
                    colorHax(secondaryColorColorPickerPreference.getColor())
                ), nightMode()
            )
        } else {
            resourceProvider.primaryThemeOverlay(ColorsTheme.DEEP_PURPLE, nightMode())
        }
    }

    @StyleRes
    private fun shapeThemeOverlay(value: String): Int {
        return when (value) {
            "1" -> resourceProvider.shapeThemeOverlay(ShapesTheme.ROUNDED)
            "2" -> resourceProvider.shapeThemeOverlay(ShapesTheme.CUT)
            else -> resourceProvider.shapeThemeOverlay(ShapesTheme.ROUNDED)
        }
    }

    @StyleRes
    private fun shapeSizeThemeOverlay(value: String): Int {
        return when (value) {
            "1" -> resourceProvider.shapeSizeThemeOverlay(ShapeSizeTheme.SMALL)
            "2" -> resourceProvider.shapeSizeThemeOverlay(ShapeSizeTheme.MEDIUM)
            "3" -> resourceProvider.shapeSizeThemeOverlay(ShapeSizeTheme.LARGE)
            else -> resourceProvider.shapeSizeThemeOverlay(ShapeSizeTheme.SMALL)
        }
    }
}