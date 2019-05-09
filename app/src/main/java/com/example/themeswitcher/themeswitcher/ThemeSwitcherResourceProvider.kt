/*
 * Copyright 2018 The Android Open Source Project
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

import androidx.annotation.StyleRes
import com.example.themeswitcher.R
import com.example.themeswitcher.enums.ColorsTheme
import com.example.themeswitcher.enums.ShapeSizeTheme
import com.example.themeswitcher.enums.ShapesTheme

/** A helper class that facilitates overriding of theme switcher resources in the Catalog app.  */
class ThemeSwitcherResourceProvider {

    fun themeColor(colorHax: String): ColorsTheme {
        return when (colorHax) {
            "#F44336" -> ColorsTheme.RED
            "#E91E63" -> ColorsTheme.PINK
            "#9C27B0" -> ColorsTheme.PURPLE
            "#673AB7" -> ColorsTheme.DEEP_PURPLE
            "#3F51B5" -> ColorsTheme.INDIGO

            "#2196F3" -> ColorsTheme.BLUE
            "#03A9F4" -> ColorsTheme.LIGHT_BLUE
            "#00BCD4" -> ColorsTheme.CYAN
            "#009688" -> ColorsTheme.TEAL
            "#4CAF50" -> ColorsTheme.GREEN

            "#8BC34A" -> ColorsTheme.LIGHT_GREEN
            "#CDDC39" -> ColorsTheme.LIME
            "#FFEB3B" -> ColorsTheme.YELLOW
            "#FFC107" -> ColorsTheme.AMBER
            "#FF9800" -> ColorsTheme.ORANGE

            "#FF5722" -> ColorsTheme.DEEP_ORANGE
            "#795548" -> ColorsTheme.BROWN
            "#9E9E9E" -> ColorsTheme.GREY
            "#607D8B" -> ColorsTheme.BLUE_GREY
            "#000000" -> ColorsTheme.BLACK
            else -> when (colorHax) {
                "#EF9A9A" -> ColorsTheme.RED
                "#F48FB1" -> ColorsTheme.PINK
                "#CE93D8" -> ColorsTheme.PURPLE
                "#B39DDB" -> ColorsTheme.DEEP_PURPLE
                "#9FA8DA" -> ColorsTheme.INDIGO

                "#90CAF9" -> ColorsTheme.BLUE
                "#81D4fA" -> ColorsTheme.LIGHT_BLUE
                "#80DEEA" -> ColorsTheme.CYAN
                "#80CBC4" -> ColorsTheme.TEAL
                "#A5D6A7" -> ColorsTheme.GREEN

                "#C5E1A5" -> ColorsTheme.LIGHT_GREEN
                "#E6EE9C" -> ColorsTheme.LIME
                "#FFF590" -> ColorsTheme.YELLOW
                "#FFE082" -> ColorsTheme.AMBER
                "#FFCC80" -> ColorsTheme.ORANGE

                "#FFAB91" -> ColorsTheme.DEEP_ORANGE
                "#BCAAA4" -> ColorsTheme.BROWN
                "#EEEEEE" -> ColorsTheme.GREY
                "#B0BBC5" -> ColorsTheme.BLUE_GREY
                "#FFFFFF" -> ColorsTheme.BLACK
                else -> ColorsTheme.INDIGO
            }
        }
    }

    fun colorInverse(colorHax: String): String {
        return when (colorHax) {
            "#F44336" -> "#EF9A9A"
            "#E91E63" -> "#F48FB1"
            "#9C27B0" -> "#CE93D8"
            "#673AB7" -> "#B39DDB"
            "#3F51B5" -> "#9FA8DA"

            "#2196F3" -> "#90CAF9"
            "#03A9F4" -> "#81D4fA"
            "#00BCD4" -> "#80DEEA"
            "#009688" -> "#80CBC4"
            "#4CAF50" -> "#A5D6A7"

            "#8BC34A" -> "#C5E1A5"
            "#CDDC39" -> "#E6EE9C"
            "#FFEB3B" -> "#FFF590"
            "#FFC107" -> "#FFE082"
            "#FF9800" -> "#FFCC80"

            "#FF5722" -> "#FFAB91"
            "#795548" -> "#BCAAA4"
            "#9E9E9E" -> "#EEEEEE"
            "#607D8B" -> "#B0BBC5"
            "#FFFFFF" -> "#000000"

            /*==Night mode colors==*/

            "#EF9A9A" -> "#F44336"
            "#F48FB1" -> "#E91E63"
            "#CE93D8" -> "#9C27B0"
            "#B39DDB" -> "#673AB7"
            "#9FA8DA" -> "#3F51B5"

            "#90CAF9" -> "#2196F3"
            "#81D4fA" -> "#03A9F4"
            "#80DEEA" -> "#00BCD4"
            "#80CBC4" -> "#009688"
            "#A5D6A7" -> "#4CAF50"

            "#C5E1A5" -> "#8BC34A"
            "#E6EE9C" -> "#CDDC39"
            "#FFF590" -> "#FFEB3B"
            "#FFE082" -> "#FFC107"
            "#FFCC80" -> "#FF9800"

            "#FFAB91" -> "#FF5722"
            "#BCAAA4" -> "#795548"
            "#EEEEEE" -> "#9E9E9E"
            "#B0BBC5" -> "#607D8B"
            "#000000" -> "#FFFFFF"
            else -> "#3F51B5"
        }
    }

    @StyleRes
    fun primaryThemeOverlay(color: ColorsTheme, nightMode: Boolean): Int {
        return if (nightMode) {
            when (color) {
                ColorsTheme.RED -> R.style.ThemeOverlay_PrimaryPalette_Red_Night
                ColorsTheme.PINK -> R.style.ThemeOverlay_PrimaryPalette_Pink_Night
                ColorsTheme.PURPLE -> R.style.ThemeOverlay_PrimaryPalette_Purple_Night
                ColorsTheme.DEEP_PURPLE -> R.style.ThemeOverlay_PrimaryPalette_DeepPurple_Night
                ColorsTheme.INDIGO -> R.style.ThemeOverlay_PrimaryPalette_Indigo_Night
                ColorsTheme.BLUE -> R.style.ThemeOverlay_PrimaryPalette_Blue_Night
                ColorsTheme.LIGHT_BLUE -> R.style.ThemeOverlay_PrimaryPalette_LightBlue_Night
                ColorsTheme.CYAN -> R.style.ThemeOverlay_PrimaryPalette_Cyan_Night
                ColorsTheme.TEAL -> R.style.ThemeOverlay_PrimaryPalette_Teal_Night
                ColorsTheme.GREEN -> R.style.ThemeOverlay_PrimaryPalette_Green_Night
                ColorsTheme.LIGHT_GREEN -> R.style.ThemeOverlay_PrimaryPalette_LightGreen_Night
                ColorsTheme.LIME -> R.style.ThemeOverlay_PrimaryPalette_Lime_Night
                ColorsTheme.YELLOW -> R.style.ThemeOverlay_PrimaryPalette_Yellow_Night
                ColorsTheme.AMBER -> R.style.ThemeOverlay_PrimaryPalette_Amber_Night
                ColorsTheme.ORANGE -> R.style.ThemeOverlay_PrimaryPalette_Orange_Night
                ColorsTheme.DEEP_ORANGE -> R.style.ThemeOverlay_PrimaryPalette_DeepOrange_Night
                ColorsTheme.BROWN -> R.style.ThemeOverlay_PrimaryPalette_Brown_Night
                ColorsTheme.GREY -> R.style.ThemeOverlay_PrimaryPalette_Grey_Night
                ColorsTheme.BLUE_GREY -> R.style.ThemeOverlay_PrimaryPalette_BlueGrey_Night
                ColorsTheme.BLACK -> R.style.ThemeOverlay_PrimaryPalette_Black_Night
            }
        } else {
            when (color) {
                ColorsTheme.RED -> R.style.ThemeOverlay_PrimaryPalette_Red
                ColorsTheme.PINK -> R.style.ThemeOverlay_PrimaryPalette_Pink
                ColorsTheme.PURPLE -> R.style.ThemeOverlay_PrimaryPalette_Purple
                ColorsTheme.DEEP_PURPLE -> R.style.ThemeOverlay_PrimaryPalette_DeepPurple
                ColorsTheme.INDIGO -> R.style.ThemeOverlay_PrimaryPalette_Indigo
                ColorsTheme.BLUE -> R.style.ThemeOverlay_PrimaryPalette_Blue
                ColorsTheme.LIGHT_BLUE -> R.style.ThemeOverlay_PrimaryPalette_LightBlue
                ColorsTheme.CYAN -> R.style.ThemeOverlay_PrimaryPalette_Cyan
                ColorsTheme.TEAL -> R.style.ThemeOverlay_PrimaryPalette_Teal
                ColorsTheme.GREEN -> R.style.ThemeOverlay_PrimaryPalette_Green
                ColorsTheme.LIGHT_GREEN -> R.style.ThemeOverlay_PrimaryPalette_LightGreen
                ColorsTheme.LIME -> R.style.ThemeOverlay_PrimaryPalette_Lime
                ColorsTheme.YELLOW -> R.style.ThemeOverlay_PrimaryPalette_Yellow
                ColorsTheme.AMBER -> R.style.ThemeOverlay_PrimaryPalette_Amber
                ColorsTheme.ORANGE -> R.style.ThemeOverlay_PrimaryPalette_Orange
                ColorsTheme.DEEP_ORANGE -> R.style.ThemeOverlay_PrimaryPalette_DeepOrange
                ColorsTheme.BROWN -> R.style.ThemeOverlay_PrimaryPalette_Brown
                ColorsTheme.GREY -> R.style.ThemeOverlay_PrimaryPalette_Grey
                ColorsTheme.BLUE_GREY -> R.style.ThemeOverlay_PrimaryPalette_BlueGrey
                ColorsTheme.BLACK -> R.style.ThemeOverlay_PrimaryPalette_Black
            }
        }
    }

    @StyleRes
    fun shapeThemeOverlay(shape: ShapesTheme): Int {
        return when (shape) {
            ShapesTheme.ROUNDED -> R.style.ThemeOverlay_Shapes_Rounded
            ShapesTheme.CUT -> R.style.ThemeOverlay_Shapes_Cut
        }
    }

    @StyleRes
    fun shapeSizeThemeOverlay(shapeSize: ShapeSizeTheme): Int {
        return when (shapeSize) {
            ShapeSizeTheme.SMALL -> R.style.ThemeOverlay_ShapeSize_Small
            ShapeSizeTheme.MEDIUM -> R.style.ThemeOverlay_ShapeSize_Medium
            ShapeSizeTheme.LARGE -> R.style.ThemeOverlay_ShapeSize_Large
        }
    }
}
